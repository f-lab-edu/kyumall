package com.kyumall.kyumallclient.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.member.MemberFactory;
import com.kyumall.kyumallclient.pay.PayOpenFeign;
import com.kyumall.kyumallclient.pay.PayResponse;
import com.kyumall.kyumallclient.product.ProductFactory;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import com.kyumall.kyumallcommon.order.entity.OrderGroup;
import com.kyumall.kyumallcommon.order.entity.Orders;
import com.kyumall.kyumallcommon.order.repository.OrderRepository;
import com.kyumall.kyumallcommon.order.vo.OrderStatus;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.StockRepository;
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
  private static final String pw = "12341234";
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private OrderRepository orderRepository;
  @Autowired
  private StockRepository stockRepository;
  @MockBean
  private PayOpenFeign payOpenFeign;

  Member member01;
  Product apple;
  Product banana;

  @BeforeEach
  void dataInit() {
    member01 = memberFactory.createMember("test01", "test01@example.com", pw, MemberType.CLIENT);
    apple = productFactory.createProduct("apple", 10000);
    banana = productFactory.createProduct("banana", 20000);
  }

  @Test
  @DisplayName("주문 생성에 성공합니다.")
  void createTest_success() {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    int count = 10;
    CreateOrderGroupRequest request = CreateOrderGroupRequest.builder()
        .productIdAndCounts(List.of(
            new ProductIdAndCount(apple.getId(), count),
            new ProductIdAndCount(banana.getId(), count)))
        .build();

    ExtractableResponse<Response> response = requestCreateOrder(spec, request);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    OrderGroup orderGroup = findOrderGroup(response);
    assertThat(orderGroup.getBuyer().getId()).isEqualTo(member01.getId());
    assertThat(orderGroup.getOrders()).hasSize(2);
    assertThat(orderGroup.getOrders().get(0).getProduct().getId()).isEqualTo(apple.getId());
    assertThat(orderGroup.getOrders().get(1).getProduct().getId()).isEqualTo(banana.getId());
    assertThat(orderGroup.getOrderNumber()).isNotEmpty();
    List<Orders> orders = orderGroup.getOrders();
    assertThat(orders).hasSize(2);
    assertThat(orders.get(0).getOrderStatus()).isEqualTo(OrderStatus.BEFORE_PAY);
    assertThat(orders.get(0).getCount()).isEqualTo(count);
    assertThat(orders.get(1).getOrderStatus()).isEqualTo(OrderStatus.BEFORE_PAY);
    assertThat(orders.get(1).getCount()).isEqualTo(count);
  }

  @Test
  @DisplayName("주문 결재에 성공합니다.")
  void payOrder_complete() {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    // given
    // 재고 추가
    Long appleStock = 20L;
    Long bananaStock = 30L;
    productFactory.changeStock(apple.getId(), appleStock, spec);
    productFactory.changeStock(banana.getId(), bananaStock, spec);
    // 주문 생성
    Integer appleOrderQuantity = 10;
    Integer bananaOrderQuantity = 10;
    CreateOrderGroupRequest createRequest = CreateOrderGroupRequest.builder()
        .productIdAndCounts(List.of(
            new ProductIdAndCount(apple.getId(), appleOrderQuantity),
            new ProductIdAndCount(banana.getId(), bananaOrderQuantity)))
        .build();
    OrderGroup createdOrderGroup = findOrderGroup(requestCreateOrder(spec, createRequest));
    // mock
    given(payOpenFeign.pay(anyLong(), anyLong())).willReturn(new PayResponse("success", "성공"));

    // when
    ExtractableResponse<Response> response = requestPayOrder(spec, createdOrderGroup);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    OrderGroup orderGroup = findOrderGroup(createdOrderGroup.getId());
    for (Orders order: orderGroup.getOrders()) {
      assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAY_COMPLETE);
    }
    assertThat(stockRepository.findByProduct(apple).orElseThrow().getQuantity())
        .isEqualTo(appleStock - appleOrderQuantity);
    assertThat(stockRepository.findByProduct(banana).orElseThrow().getQuantity())
        .isEqualTo(bananaStock - bananaOrderQuantity);
  }

  private static ExtractableResponse<Response> requestPayOrder(RequestSpecification spec,
      OrderGroup createdOrderGroup) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", createdOrderGroup.getId())
        .when().post("/orders/{id}/pay")
        .then().log().all()
        .extract();
  }

  @Test
  @DisplayName("재고가 부족하여 주문 결제에 실패합니다.")
  void payOrder_fail_because_of_insufficient_stock() {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    // given
    // 재고 추가
    Long appleStock = 10L;
    Long bananaStock = 10L;
    productFactory.changeStock(apple.getId(), appleStock, spec);
    productFactory.changeStock(banana.getId(), bananaStock, spec);
    // 주문 생성
    Integer appleOrderQuantity = 11;
    Integer bananaOrderQuantity = 10;
    CreateOrderGroupRequest createRequest = CreateOrderGroupRequest.builder()
        .productIdAndCounts(List.of(
            new ProductIdAndCount(apple.getId(), appleOrderQuantity),
            new ProductIdAndCount(banana.getId(), bananaOrderQuantity)))
        .build();
    OrderGroup createdOrderGroup = findOrderGroup(requestCreateOrder(spec, createRequest));
    // mock
    given(payOpenFeign.pay(anyLong(), anyLong())).willReturn(new PayResponse("success", "성공"));

    // when
    ExtractableResponse<Response> response = requestPayOrder(spec, createdOrderGroup);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("주문 결제 시, 재고 감소 동시성 테스트")
  void payOrder_concurrency_test() throws InterruptedException {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(member01.getUsername(), pw);
    // given
    // 재고 추가
    Long appleStock = 100L;
    productFactory.changeStock(apple.getId(), appleStock, spec);
    // 주문 다건 생성
    int orderCount = 100;
    List<OrderGroup> createOrderGroups = new ArrayList<>();
    for (int i = 0; i < orderCount; i++) {
      Integer appleOrderQuantity = 1;
      CreateOrderGroupRequest createRequest = CreateOrderGroupRequest.builder()
          .productIdAndCounts(List.of(
              new ProductIdAndCount(apple.getId(), appleOrderQuantity)))
          .build();
      createOrderGroups.add(findOrderGroup(requestCreateOrder(spec, createRequest)));
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
          requestPayOrder(spec, createOrderGroups.get(idx));
        } finally {
          latch.countDown();
        }
      });
    }
    latch.await();

    // then
    assertThat(stockRepository.findByProduct(apple).orElseThrow().getQuantity()).isEqualTo(0);
  }

  private static ExtractableResponse<Response> requestCreateOrder(RequestSpecification spec,
      CreateOrderGroupRequest request) {
    return RestAssured.given().log().all().spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/orders")
        .then().log().all()
        .extract();
  }

  private OrderGroup findOrderGroup(ExtractableResponse<Response> response) {
    return findOrderGroup(response.body().jsonPath().getLong("result.id"));
  }

  private OrderGroup findOrderGroup(Long id) {
    return orderRepository.findWithOrdersById(id)
        .orElseThrow(() -> new RuntimeException("test fail"));
  }
}
