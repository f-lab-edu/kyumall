package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.product.cart.AddCartItemRequest;
import com.kyumall.kyumallclient.product.cart.dto.CartItemsDto;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.entity.CartItem;
import com.kyumall.kyumallcommon.product.entity.Category;
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
import org.springframework.stereotype.Component;

@Component
@DisplayName("카트 기능 통합테스트")
class CartIntegrationTest extends IntegrationTest {
  @Autowired
  ProductFactory productFactory;
  @Autowired
  MemberFactory memberFactory;
  @Autowired
  MemberRepository memberRepository;

  Product apple;
  Product banana;
  private static final String password = "test1234";
  Member testMember1;

  @BeforeEach
  void dataInit() {
    // 물품
    Category testCategory = productFactory.createCategory("testCategory");
    apple = productFactory.createProduct("꿀사과", 1000);
    banana = productFactory.createProduct("바나나", 1000);
    // 회원
    testMember1 = memberFactory.createClient("test01", password);
  }

  @Test
  @DisplayName("카트에 상품 추가를 성공합니다.")
  void addCartItem_success() {
    // given
    AddCartItemRequest request = AddCartItemRequest.builder()
        .productId(apple.getId())
        .count(1)
        .build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);

    // when
    ExtractableResponse<Response> response = requestAddCartItem(request, spec);
    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
  }

  @Test
  @DisplayName("카트에 중복된 상품 추가시, 수량이 늘어납니다.")
  void addCartItem_duplicate_item_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    AddCartItemRequest request = AddCartItemRequest.builder()
        .productId(apple.getId())
        .count(1)
        .build();
    requestAddCartItem(request, spec);

    // when
    ExtractableResponse<Response> response = requestAddCartItem(request, spec);
    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<CartItem> cartItems = findMemberWithCart(testMember1.getId()).getCart().getCartItems();
    assertThat(cartItems).hasSize(1);
    assertThat(cartItems.get(0).getProduct().getId()).isEqualTo(apple.getId());
    assertThat(cartItems.get(0).getCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("카트에 담긴 상품을 조회합니다.")
  void getCartItems_success() {
    // given
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    addCartItemForGiven(testMember1.getUsername(), banana.getId(), 3);


    ExtractableResponse<Response> response = requestGetCartItems(testMember1.getUsername());

    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<CartItemsDto> items = response.body().jsonPath().getList("result", CartItemsDto.class);
    assertThat(items).hasSize(2);
    // 사과
    assertCartItemsDto(items, 0, apple, 1);
    // 바나나
    assertCartItemsDto(items, 1, banana, 3);
  }



  private void assertCartItemsDto(List<CartItemsDto> items, int index, Product product, int count) {
    assertThat(items.get(index).getProductId()).isEqualTo(product.getId());
    assertThat(items.get(index).getProductName()).isEqualTo(product.getName());
    assertThat(items.get(index).getImage()).isEqualTo(product.getImage());
    assertThat(items.get(index).getPrice()).isEqualTo(product.getPrice());
    assertThat(items.get(index).getCount()).isEqualTo(count);
  }

  @Test
  @DisplayName("카트에서 상품 하나 제거를 성공합니다.")
  void deleteCartItem_success() {
    // given
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    addCartItemForGiven(testMember1.getUsername(), banana.getId(), 1);
    List<CartItemsDto> cartItems = requestGetCartItemsAndGetDto(testMember1.getUsername());
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    List<Long> IdsToDelete = List.of(cartItems.get(0).getCartItemId());

    // when
    ExtractableResponse<Response> response = requestDeleteCartItems(spec, IdsToDelete);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Member member = findMemberWithCart(testMember1.getId());
    List<CartItem> actualCartItems = member.getCart().getCartItems();
    assertThat(actualCartItems).hasSize(1);
    assertThat(actualCartItems.get(0).getProduct().getId()).isEqualTo(banana.getId());
  }

  @Test
  @DisplayName("카트에서 상품 두개 제거를 성공합니다.")
  void deleteCartItem_two_items_success() {
    // given
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    addCartItemForGiven(testMember1.getUsername(), banana.getId(), 1);
    List<CartItemsDto> cartItems = requestGetCartItemsAndGetDto(testMember1.getUsername());
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    List<Long> IdsToDelete = List.of(
        cartItems.get(0).getCartItemId(), cartItems.get(1).getCartItemId());

    // when
    ExtractableResponse<Response> response = requestDeleteCartItems(spec, IdsToDelete);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Member member = findMemberWithCart(testMember1.getId());
    List<CartItem> actualCartItems = member.getCart().getCartItems();
    assertThat(actualCartItems).isEmpty();
  }

  @Test
  @DisplayName("카트에 담긴 상품의 갯수를 수정합니다.")
  void adjustCartItemCount_success() {
    // given
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    List<CartItemsDto> cartItems = requestGetCartItemsAndGetDto(testMember1.getUsername());
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    CartItemsDto cartItemsToAdjust = cartItems.get(0);
    int countToAdjust = 3;

    ExtractableResponse<Response> response = RestAssured.given().log().all().spec(spec)
        .pathParam("id", cartItemsToAdjust.getCartItemId())
        .queryParam("count", countToAdjust)
        .when().put("/carts/cartItems/{id}/adjust-count")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Member memberWithCart = findMemberWithCart(testMember1.getId());
    CartItem cartItem = memberWithCart.getCart().getCartItem(cartItemsToAdjust.getCartItemId())
            .orElseThrow(() -> new RuntimeException("test fail"));
    assertThat(cartItem.getCount()).isEqualTo(countToAdjust);
  }

  @Test
  @DisplayName("카트에 담긴 상품의 갯수를 수정합니다.")
  void adjustCartItemCount_fail_because_of_minus_count() {
    // given
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    List<CartItemsDto> cartItems = requestGetCartItemsAndGetDto(testMember1.getUsername());
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    CartItemsDto cartItemsToAdjust = cartItems.get(0);
    int countToAdjust = -1;

    ExtractableResponse<Response> response = RestAssured.given().log().all().spec(spec)
        .pathParam("id", cartItemsToAdjust.getCartItemId())
        .queryParam("count", countToAdjust)
        .when().put("/carts/cartItems/{id}/adjust-count")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  private Member findMemberWithCart(Long memberId) {
    return memberRepository.findWithCartById(memberId)
        .orElseThrow(() -> new RuntimeException("test fail"));
  }

  private static ExtractableResponse<Response> requestDeleteCartItems(RequestSpecification spec,
      List<Long> IdsToDelete) {
    return RestAssured.given().log().all().spec(spec)
        .contentType(ContentType.JSON)
        .body(IdsToDelete)
        .when().delete("/carts/cartItems")
        .then().log().all()
        .extract();
  }

  private static void addCartItemForGiven(String username, Long productId, int count) {
    AddCartItemRequest request = AddCartItemRequest.builder()
        .productId(productId)
        .count(count)
        .build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(username, password);

    requestAddCartItem(request, spec);
  }

  public static List<CartItemsDto> requestGetCartItemsAndGetDto(String username) {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(username, password);

    ExtractableResponse<Response> response = requestGetCartItems(username);

    return response.body().jsonPath().getList("result", CartItemsDto.class);
  }

  public static ExtractableResponse<Response> requestGetCartItems(String username) {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(username, password);

    return RestAssured.given().log().all().spec(spec)
        .when().get("/carts/cartItems")
        .then().log().all()
        .extract();
  }

  public static ExtractableResponse<Response> requestAddCartItem(AddCartItemRequest request,
      RequestSpecification spec) {
    return RestAssured.given().log().all().spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/carts/cartItems")
        .then().log().all()
        .extract();
  }
}
