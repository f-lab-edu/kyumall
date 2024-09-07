package com.kyumall.kyumalladmin.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.product.dto.CreateProductRequest;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
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

  Member seller;

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    seller = memberFactory.createMember(MemberFixture.LEE);
  }

  @Test
  @DisplayName("상품 생성에 성공합니다.")
  void createProduct_success() {
    // given
    Category fruit = productFactory.createCategory(CategoryFixture.FRUIT);
    CreateProductRequest request = CreateProductRequest.builder()
        .productName("얼음골 사과")
        .categoryId(fruit.getId())
        .sellerUsername(seller.getUsername())
        .price(30000)
        .detail("사과입니다.").build();
    // when
    ExtractableResponse<Response> response = requestCreateProduct(
        request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Integer createdId = response.body().jsonPath().get("result.id");
    Product savedProduct = findProductById(createdId.longValue());
    assertThat(savedProduct.getName()).isEqualTo(request.getProductName());
    assertThat(savedProduct.getCategory().getId()).isEqualTo(request.getCategoryId());
    assertThat(savedProduct.getSeller().getId()).isEqualTo(seller.getId());
    assertThat(savedProduct.getPrice()).isEqualTo(request.getPrice());
    assertThat(savedProduct.getDetail()).isEqualTo(request.getDetail());
  }

  private static ExtractableResponse<Response> requestCreateProduct(
      CreateProductRequest request) {
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/products")
        .then().log().all()
        .extract();
    return response;
  }

  private Product findProductById(Long productId) {
    return productRepository.findWithFetchById(productId)
        .orElseThrow(() -> new RuntimeException());
  }
}
