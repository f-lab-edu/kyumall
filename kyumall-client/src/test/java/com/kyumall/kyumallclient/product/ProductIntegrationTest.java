package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.member.MemberFactory;
import com.kyumall.kyumallclient.product.dto.CreateProductRequest;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("상품 통합테스트")
class ProductIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ProductRepository productRepository;

  Member seller01;
  Category food;
  Category fruit;
  Category meet;
  Product apple;
  Product beef;
  List<Product> allProducts = new ArrayList<>();

  @BeforeEach
  void initData() {
    seller01 = memberFactory.createMember("user01", "email@example.com", "password", MemberType.SELLER);
    food = categoryRepository.save(Category.builder()
        .name("식품")
        .status(CategoryStatus.INUSE)
        .build());
    fruit = categoryRepository.save(Category.builder()
        .name("과일")
        .parent(food)
        .status(CategoryStatus.INUSE)
        .build());
    meet = categoryRepository.save(Category.builder()
        .name("육류")
        .parent(food)
        .status(CategoryStatus.INUSE)
        .build());
    apple = createProductForTest(new CreateProductRequest("얼음골사과", fruit.getId(), seller01.getUsername() ,40000,
        "<h1>맛있는 사과</h1>"));
    beef = createProductForTest(new CreateProductRequest("소고기", meet.getId(), seller01.getUsername() ,50000,
        "<h1>맛있는 소고기</h1>"));
    allProducts.add(apple);
    allProducts.add(beef);
  }

  @Test
  @DisplayName("상품 생성에 성공합니다.")
  void createProduct_success() {
    // given
    CreateProductRequest request = CreateProductRequest.builder()
        .productName("얼음골 사과")
        .categoryId(fruit.getId())
        .sellerUsername(seller01.getUsername())
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
    assertThat(savedProduct.getSeller().getId()).isEqualTo(seller01.getId());
    assertThat(savedProduct.getPrice()).isEqualTo(request.getPrice());
    assertThat(savedProduct.getDetail()).isEqualTo(request.getDetail());
  }

  @Test
  @DisplayName("상품리스트 조회에 성공합니다.")
  void getAllProducts_success() {
    //given
    //when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .when().get("/products")
        .then().log().all()
        .extract();
    //then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    // 페이징 검증
    assertThat((Integer)response.body().jsonPath().get("result.size")).isEqualTo(10);
    assertThat((Integer)response.body().jsonPath().get("result.number")).isEqualTo(0);
    // 조회 결과 검증
    List<ProductSimpleDto> result = response.body().jsonPath().getList("result.content", ProductSimpleDto.class);
    assertThat(result).hasSize(2);
    for (ProductSimpleDto dto: result) {
      assertThat(dto).usingRecursiveComparison().comparingOnlyFields("name", "price", "image").isIn(allProducts);
    }
  }

  private Product createProductForTest(CreateProductRequest request) {
    Integer newProductId = requestCreateProduct(request).body().jsonPath().get("result.id");
    return findProductById(newProductId.longValue());
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
    return productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException());
  }
}
