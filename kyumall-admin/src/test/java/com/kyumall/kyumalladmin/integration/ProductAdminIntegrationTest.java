package com.kyumall.kyumalladmin.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.kyumall.kyumalladmin.AuthTestUtil;
import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.product.dto.ProductForm;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductAdminIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private CategoryRepository categoryRepository;

  Member adminMike;
  Member adminBilly;

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    adminMike = memberFactory.createMember(MemberFixture.MIKE);
    adminBilly = memberFactory.createMember(MemberFixture.BILLY);
  }

  @Test
  @DisplayName("상품 생성에 성공합니다.")
  void createProduct_success() {
    // given
    Category fruit = productFactory.createCategory(CategoryFixture.FRUIT);
    ProductForm request = ProductForm.builder()
        .productName("얼음골 사과")
        .categoryId(fruit.getId())
        .price(30000)
        .detail("사과입니다.").build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    // when
    ExtractableResponse<Response> response = requestCreateProduct(spec, request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Integer createdId = response.body().jsonPath().get("result.id");
    Product savedProduct = findProductById(createdId.longValue());
    assertThat(savedProduct.getName()).isEqualTo(request.getProductName());
    assertThat(savedProduct.getCategory().getId()).isEqualTo(request.getCategoryId());
    assertThat(savedProduct.getSeller().getId()).isEqualTo(adminMike.getId());
    assertThat(savedProduct.getPrice()).isEqualTo(request.getPrice());
    assertThat(savedProduct.getDetail()).isEqualTo(request.getDetail());
  }

  @Test
  @DisplayName("상품의 정보 변경에 성공합니다.")
  void updateProduct_success() {
    Category category1 = categoryRepository.saveAndFlush(CategoryFixture.FOOD.toEntity());
    Category category2 = categoryRepository.saveAndFlush(CategoryFixture.FASHION.toEntity());
    Product product = productRepository.saveAndFlush(ProductFixture.APPLE.toEntity(adminMike, category1));
    ProductForm productForm = ProductForm.builder()
        .productName("updated name")
        .price(7777)
        .detail("updated details")
        .categoryId(category2.getId())
        .build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    // when
    ExtractableResponse<Response> response = requestUpdateProduct(spec, product.getId(), productForm);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Product productSaved = productRepository.findById(product.getId()).orElseThrow();
    assertThat(productSaved.getName()).isEqualTo(productForm.getProductName());
    assertThat(productSaved.getPrice()).isEqualTo(productForm.getPrice());
    assertThat(productSaved.getDetail()).isEqualTo(productForm.getDetail());
    assertThat(productSaved.getCategory().getId()).isEqualTo(productForm.getCategoryId());
  }

  @Test
  @DisplayName("상품을 등록한 관리자가 아니라서 상품 변경에 실패합니다.")
  void updateProduct_fail_because_of_not_register() {
    Category category1 = categoryRepository.saveAndFlush(CategoryFixture.FOOD.toEntity());
    Category category2 = categoryRepository.saveAndFlush(CategoryFixture.FASHION.toEntity());
    Product product = productRepository.saveAndFlush(ProductFixture.APPLE.toEntity(adminMike, category1));
    ProductForm productForm = ProductForm.builder()
        .productName("updated name")
        .price(7777)
        .detail("updated details")
        .categoryId(category2.getId())
        .build();
    // 상품 등록자 아님
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminBilly.getUsername(), MemberFixture.password);

    // when
    ExtractableResponse<Response> response = requestUpdateProduct(spec, product.getId(), productForm);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
  }

  private static ExtractableResponse<Response> requestCreateProduct(RequestSpecification spec, ProductForm request) {
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/products")
        .then().log().all()
        .extract();
    return response;
  }

  private static ExtractableResponse<Response> requestUpdateProduct(RequestSpecification spec, Long productId, ProductForm request) {
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .spec(spec)
        .body(request)
        .pathParam("id", productId)
        .when().put("/products/{id}")
        .then().log().all()
        .extract();
    return response;
  }

  private Product findProductById(Long productId) {
    return productRepository.findWithFetchById(productId)
        .orElseThrow(() -> new RuntimeException());
  }
}
