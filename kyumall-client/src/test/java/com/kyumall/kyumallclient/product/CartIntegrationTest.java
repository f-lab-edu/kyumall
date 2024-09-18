package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallcommon.product.cart.Cart;
import com.kyumall.kyumallcommon.product.cart.CartRepository;
import com.kyumall.kyumallcommon.product.cart.dto.AddCartItemRequest;
import com.kyumall.kyumallcommon.product.cart.dto.CartItemsDto;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.cart.CartItem;
import com.kyumall.kyumallcommon.product.product.entity.Product;
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
  @Autowired
  CartRepository cartRepository;

  private static final String password = MemberFixture.password;
  Member seller;
  Member testMember1;

  @BeforeEach
  void dataInit() {
    seller = memberFactory.createMember(MemberFixture.BILLY);
    testMember1 = memberFactory.createMember(MemberFixture.KIM);
  }

  @Test
  @DisplayName("카트에 상품 추가를 성공합니다.")
  void addCartItem_success() {
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
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
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
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
    List<CartItem> cartItems = findCartByMemberId(testMember1.getId()).getCartItems();
    assertThat(cartItems).hasSize(1);
    assertThat(cartItems.get(0).getProduct().getId()).isEqualTo(apple.getId());
    assertThat(cartItems.get(0).getCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("카트에 담긴 상품을 조회합니다.")
  void getCartItems_success() {
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    Product beef = productFactory.createProduct(ProductFixture.BEEF, seller);
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    addCartItemForGiven(testMember1.getUsername(), beef.getId(), 3);

    ExtractableResponse<Response> response = requestGetCartItems(testMember1.getUsername());

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<CartItemsDto> items = response.body().jsonPath().getList("result", CartItemsDto.class);
    assertThat(items).hasSize(2);
    // 사과
    assertCartItemsDto(items, 0, apple, 1);
    // 바나나
    assertCartItemsDto(items, 1, beef, 3);
  }

  @Test
  @DisplayName("장바구니의 항목이 삭제된 상품인 경우, 갯수가 0으로 표시됩니다.")
  void getCartItems_count_0_when_deleted_product() {
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    Product beef = productFactory.createProduct(ProductFixture.BEEF, seller);
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    addCartItemForGiven(testMember1.getUsername(), beef.getId(), 3);
    // apple 상품을 삭제처리
    productFactory.saveProduct(apple.delete());

    // when 상품 조회
    ExtractableResponse<Response> response = requestGetCartItems(testMember1.getUsername());

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<CartItemsDto> items = response.body().jsonPath().getList("result", CartItemsDto.class);
    assertThat(items.get(0).getCount()).isZero();
    assertThat(items.get(0).getIsDeleted()).isTrue();
    assertThat(items.get(1).getCount()).isEqualTo(3);
    assertThat(items.get(1).getIsDeleted()).isFalse();
  }

  private void assertCartItemsDto(List<CartItemsDto> items, int index, Product product, int count) {
    assertThat(items.get(index).getProductId()).isEqualTo(product.getId());
    assertThat(items.get(index).getProductName()).isEqualTo(product.getName());
    assertThat(items.get(index).getImage()).isEqualTo(product.getRepresentativeImage());
    assertThat(items.get(index).getPrice()).isEqualTo(product.getPrice());
    assertThat(items.get(index).getCount()).isEqualTo(count);
  }

  @Test
  @DisplayName("카트에서 상품 하나 제거를 성공합니다.")
  void deleteCartItem_success() {
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    Product beef = productFactory.createProduct(ProductFixture.BEEF, seller);
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    addCartItemForGiven(testMember1.getUsername(), beef.getId(), 1);
    List<CartItemsDto> cartItems = requestGetCartItemsAndGetDto(testMember1.getUsername());
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    List<Long> IdsToDelete = List.of(cartItems.get(0).getCartItemId());

    // when
    ExtractableResponse<Response> response = requestDeleteCartItems(spec, IdsToDelete);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<CartItem> actualCartItems = findCartByMemberId(testMember1.getId()).getCartItems();
    assertThat(actualCartItems).hasSize(1);
    assertThat(actualCartItems.get(0).getProduct().getId()).isEqualTo(beef.getId());
  }

  @Test
  @DisplayName("카트에서 상품 두개 제거를 성공합니다.")
  void deleteCartItem_two_items_success() {
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
    Product beef = productFactory.createProduct(ProductFixture.BEEF, seller);
    addCartItemForGiven(testMember1.getUsername(), apple.getId(), 1);
    addCartItemForGiven(testMember1.getUsername(), beef.getId(), 1);
    List<CartItemsDto> cartItems = requestGetCartItemsAndGetDto(testMember1.getUsername());
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    List<Long> IdsToDelete = List.of(
        cartItems.get(0).getCartItemId(), cartItems.get(1).getCartItemId());

    // when
    ExtractableResponse<Response> response = requestDeleteCartItems(spec, IdsToDelete);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<CartItem> actualCartItems = findCartByMemberId(testMember1.getId()).getCartItems();
    assertThat(actualCartItems).isEmpty();
  }

  @Test
  @DisplayName("카트에 담긴 상품의 갯수를 수정합니다.")
  void adjustCartItemCount_success() {
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
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
    Cart cart = findCartByMemberId(testMember1.getId());
    CartItem cartItem = cart.getCartItem(cartItemsToAdjust.getCartItemId())
            .orElseThrow(() -> new RuntimeException("test fail"));
    assertThat(cartItem.getCount()).isEqualTo(countToAdjust);
  }

  @Test
  @DisplayName("카트에 담긴 상품의 갯수를 수정합니다.")
  void adjustCartItemCount_fail_because_of_minus_count() {
    // given
    Product apple = productFactory.createProduct(ProductFixture.APPLE, seller);
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

  private Cart findCartByMemberId(Long memberId) {
    Member member = memberRepository.findById(memberId).orElseThrow();
    return cartRepository.findWithItemsByMember(member).orElseThrow();
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
