package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.product.dto.CreateProductRequest;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("상품 통합테스트")
class ProductIntegrationTest extends IntegrationTest {

  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ProductRepository productRepository;

  Category food;
  Category fruit;

  @BeforeEach
  void initData() {
    food = categoryRepository.save(Category.builder()
        .name("식품")
        .status(CategoryStatus.INUSE)
        .build());
    fruit = categoryRepository.save(Category.builder()
        .name("과일")
        .parent(food)
        .status(CategoryStatus.INUSE)
        .build());
  }

  @Test
  @DisplayName("상품 생성에 성공합니다.")
  void createProduct_success() {
    // givne
    CreateProductRequest request = CreateProductRequest.builder()
        .productName("얼음골 사과")
        .categoryId(fruit.getId())
        .price(30000)
        .detail("사과입니다.").build();
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/products")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Integer createdId = response.body().jsonPath().get("result.id");
    Product savedProduct = findProductById(createdId.longValue());
    assertThat(savedProduct.getName()).isEqualTo(request.getProductName());
    assertThat(savedProduct.getCategory().getId()).isEqualTo(request.getCategoryId());
    assertThat(savedProduct.getPrice()).isEqualTo(request.getPrice());
    assertThat(savedProduct.getDetail()).isEqualTo(request.getDetail());
  }

  private Product findProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException());
  }
}
