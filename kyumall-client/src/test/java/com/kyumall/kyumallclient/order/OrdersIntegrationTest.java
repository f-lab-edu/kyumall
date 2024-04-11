package com.kyumall.kyumallclient.order;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.member.MemberFactory;
import com.kyumall.kyumallclient.product.ProductFactory;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import com.kyumall.kyumallcommon.order.entity.Orders;
import com.kyumall.kyumallcommon.order.repository.OrderRepository;
import com.kyumall.kyumallcommon.order.vo.OrderStatus;
import com.kyumall.kyumallcommon.product.entity.Product;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrdersIntegrationTest extends IntegrationTest {
  private static final String pw = "12341234";
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private OrderRepository orderRepository;
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
    CreateOrderRequest request = CreateOrderRequest.builder()
        .productIdAndCounts(List.of(
            new ProductIdAndCount(apple.getId(), 10),
            new ProductIdAndCount(banana.getId(), 10)))
        .build();

    ExtractableResponse<Response> response = RestAssured.given().log().all().spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/orders")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Orders order = findOrder(response);
    assertThat(order.getBuyer().getId()).isEqualTo(member01.getId());
    assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.BEFORE_PAY);
    assertThat(order.getOrderItems()).hasSize(2);
    assertThat(order.getOrderItems().get(0).getProduct().getId()).isEqualTo(apple.getId());
    assertThat(order.getOrderItems().get(1).getProduct().getId()).isEqualTo(banana.getId());
  }

  private Orders findOrder(ExtractableResponse<Response> response) {
    return orderRepository.findWithOrderItemsById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException("test fail"));
  }
}
