package com.kyumall.kyumallclient.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.product.ProductIntegrationTest;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.order.dto.CreateOrderRequest;
import com.kyumall.kyumallcommon.order.dto.ProductIdAndCount;
import com.kyumall.kyumallcommon.pay.PayOpenFeign;
import com.kyumall.kyumallcommon.pay.PayResponse;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.order.entity.Orders;
import com.kyumall.kyumallcommon.order.repository.OrdersRepository;
import com.kyumall.kyumallcommon.order.entity.OrderItemStatus;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.stock.Stock;
import com.kyumall.kyumallcommon.product.stock.StockRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class OrderIntegrationTest extends IntegrationTest {
  private static final String pw = MemberFixture.password;
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private OrdersRepository ordersRepository;
  @Autowired
  private StockRepository stockRepository;
  @MockBean
  private PayOpenFeign payOpenFeign;

  Member seller;
  Member member01;

  @BeforeEach
  void dataInit() {
    seller = memberFactory.createMember(MemberFixture.LEE);
    member01 = memberFactory.createMember(MemberFixture.KIM);
  }

  @Test
  @DisplayName("주문 생성에 성공합니다.")
  void createTest_success() {
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    Product beef = productFactory.createProduct(ProductFixture.BEEF, seller);
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    CreateOrderRequest request = CreateOrderRequest.builder()
        .productIdAndCounts(List.of(
            new ProductIdAndCount(apple.getId(), 10),
            new ProductIdAndCount(beef.getId(), 10)))
        .build();

    ExtractableResponse<Response> response = requestCreateOrder(spec, request);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Orders order = findOrder(response);
    assertThat(order.getBuyer().getId()).isEqualTo(member01.getId());
    assertThat(order.getOrderItems().get(0).getOrderItemStatus()).isEqualTo(OrderItemStatus.BEFORE_PAY);
    assertThat(order.getOrderItems().get(1).getOrderItemStatus()).isEqualTo(OrderItemStatus.BEFORE_PAY);
    assertThat(order.getOrderItems()).hasSize(2);
    assertThat(order.getOrderItems().get(0).getProduct().getId()).isEqualTo(apple.getId());
    assertThat(order.getOrderItems().get(1).getProduct().getId()).isEqualTo(beef.getId());
  }

  @Test
  @DisplayName("주문 결재에 성공합니다.")
  void payOrder_complete() {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    Product beef = productFactory.createProduct(ProductFixture.BEEF, seller);
    // 재고 추가
    Long appleStock = 20L;
    Long beefStock = 30L;
    saveStock(apple, appleStock);
    saveStock(beef, beefStock);
    // 주문 생성
    Integer appleOrderQuantity = 10;
    Integer beefOrderQuantity = 10;
    CreateOrderRequest createRequest = CreateOrderRequest.builder()
        .productIdAndCounts(List.of(
            new ProductIdAndCount(apple.getId(), appleOrderQuantity),
            new ProductIdAndCount(beef.getId(), beefOrderQuantity)))
        .build();
    Orders createdOrder = findOrder(requestCreateOrder(spec, createRequest));
    // mock
    given(payOpenFeign.pay(anyLong(), anyLong())).willReturn(new PayResponse("success", "성공"));

    // when
    ExtractableResponse<Response> response = requestPayOrder(spec, createdOrder);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Orders order = ordersRepository.findWithOrderItemsById(createdOrder.getId()).orElseThrow();
    assertThat(order.getOrderItems().get(0).getOrderItemStatus()).isEqualTo(OrderItemStatus.PAY_COMPLETE);
    assertThat(order.getOrderItems().get(1).getOrderItemStatus()).isEqualTo(OrderItemStatus.PAY_COMPLETE);
    assertThat(stockRepository.findByProduct(apple).orElseThrow().getQuantity())
        .isEqualTo(appleStock - appleOrderQuantity);
    assertThat(stockRepository.findByProduct(beef).orElseThrow().getQuantity())
        .isEqualTo(beefStock - beefOrderQuantity);
  }

  private static ExtractableResponse<Response> requestPayOrder(RequestSpecification spec,
      Orders createdOrder) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", createdOrder.getId())
        .when().post("/orders/{id}/pay")
        .then().log().all()
        .extract();
  }

  @Test
  @DisplayName("재고가 부족하여 주문 결제에 실패합니다.")
  void payOrder_fail_because_of_insufficient_stock() {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    Product beef = productFactory.createProduct(ProductFixture.BEEF, seller);
    // 재고 추가
    Long appleStock = 10L;
    Long bananaStock = 10L;
    saveStock(apple, appleStock);
    saveStock(beef, bananaStock);
    // 주문 생성
    Integer appleOrderQuantity = 11;
    Integer bananaOrderQuantity = 10;
    CreateOrderRequest createRequest = CreateOrderRequest.builder()
        .productIdAndCounts(List.of(
            new ProductIdAndCount(apple.getId(), appleOrderQuantity),
            new ProductIdAndCount(beef.getId(), bananaOrderQuantity)))
        .build();
    Orders createdOrder = findOrder(requestCreateOrder(spec, createRequest));
    // mock
    given(payOpenFeign.pay(anyLong(), anyLong())).willReturn(new PayResponse("success", "성공"));

    // when
    ExtractableResponse<Response> response = requestPayOrder(spec, createdOrder);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("주문 결제 시, 재고 감소 동시성 테스트")
  void payOrder_concurrency_test() throws InterruptedException {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    // 재고 추가
    Long appleStock = 100L;
    saveStock(apple, appleStock);
    // 주문 다건 생성
    int orderCount = 100;
    List<Orders> createOrders = new ArrayList<>();
    for (int i = 0; i < orderCount; i++) {
      Integer appleOrderQuantity = 1;
      CreateOrderRequest createRequest = CreateOrderRequest.builder()
          .productIdAndCounts(List.of(
              new ProductIdAndCount(apple.getId(), appleOrderQuantity)))
          .build();
      createOrders.add(findOrder(requestCreateOrder(spec, createRequest)));
    }
    // mock
    given(payOpenFeign.pay(anyLong(), anyLong())).willReturn(new PayResponse("success", "성공"));

    // when
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(orderCount);

    for (int i = 0; i < orderCount; i++) {
      final int idx = i;
      executorService.submit(() -> {
        try {
          requestPayOrder(spec, createOrders.get(idx));
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();

    // then
    assertThat(stockRepository.findByProduct(apple).orElseThrow().getQuantity()).isEqualTo(0);
  }

  private void saveStock(Product product, Long quantity) {
    stockRepository.saveAndFlush(Stock.builder()
            .product(product)
            .quantity(quantity)
        .build());
  }

  private static ExtractableResponse<Response> requestCreateOrder(RequestSpecification spec,
      CreateOrderRequest request) {
    return RestAssured.given().log().all().spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/orders")
        .then().log().all()
        .extract();
  }

  private Orders findOrder(ExtractableResponse<Response> response) {
    return ordersRepository.findWithOrderItemsById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException("test fail"));
  }
}
