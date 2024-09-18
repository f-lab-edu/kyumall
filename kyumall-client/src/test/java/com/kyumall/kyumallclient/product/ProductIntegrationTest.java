package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.product.category.dto.HierarchyCategoryDto;
import com.kyumall.kyumallcommon.fixture.common.ImageFixture;
import com.kyumall.kyumallcommon.product.product.dto.ProductDetailDto;
import com.kyumall.kyumallcommon.product.product.dto.ProductSimpleDto;
import com.kyumall.kyumallclient.product.category.dto.SubCategoryDto;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.category.CategoryStatus;
import com.kyumall.kyumallcommon.product.product.repository.ProductImageRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.upload.entity.Image;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("상품 통합테스트")
public class ProductIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ImageRepository imageRepository;

  Member seller;
  Image image1;
  Image image2;

  String[] comparedProductSimpleDtoFieldNames = new String[] {"name", "price", "image"};

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    seller = memberFactory.createMember(MemberFixture.LEE);
    image1 = imageRepository.saveAndFlush(ImageFixture.PIZZA_IMAGE.toEntity());
    image2 = imageRepository.saveAndFlush(ImageFixture.CHICKEN_IMAGE.toEntity());
  }

  private Category saveCategory(String name, Category parent) {
    return categoryRepository.save(Category.builder()
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build());
  }

  @Test
  @DisplayName("상품리스트 조회에 성공합니다.(대표이미지포함)")
  void getAllProducts_success() {
    //given
    List<Product> products = new ArrayList<>();
    productFactory.createProduct(ProductFixture.APPLE, seller);
    products.add(productFactory.createProduct(ProductFixture.APPLE, seller));
    products.add(productFactory.createProduct(ProductFixture.BEEF, seller));

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
      assertThat(dto).usingRecursiveComparison().comparingOnlyFields("name", "price", "image").isIn(products);
    }
  }

  @Test
  @DisplayName("모든 카테고리를 계층형 리스트 형태로 조회합니다.")
  void getAllCategoriesHierarchy_success() {
    Category houseItem = saveCategory("생활용품", null);
    Category toiletPaper = saveCategory("화장지", houseItem);
    Category wetWipe = saveCategory("물티슈", toiletPaper);
    Category paperTowel = saveCategory("키친타올", toiletPaper);

    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .when().get("/categories/hierarchy")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<HierarchyCategoryDto> categories = response.body().jsonPath().getList("result", HierarchyCategoryDto.class);
    assertThat(categories).hasSize(1);
    assertThat(categories.get(0).getName()).isEqualTo("생활용품");
    assertThat(categories.get(0).getSubCategories().get(0).getName()).isEqualTo("화장지");
    assertThat(categories.get(0).getSubCategories().get(0).getSubCategories().get(0).getName()).isEqualTo("물티슈");
    assertThat(categories.get(0).getSubCategories().get(0).getSubCategories().get(1).getName()).isEqualTo("키친타올");
  }

  @Test
  @DisplayName("모든 카테고리를 맵 형태로 조회합니다.")
  void getAllCategoriesMap_success() {
    Category houseItem = saveCategory("생활용품", null);
    Category toiletPaper = saveCategory("화장지", houseItem);
    Category wetWipe = saveCategory("물티슈", toiletPaper);
    Category paperTowel = saveCategory("키친타올", toiletPaper);

    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .when().get("/categories/map")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Map<Long, List> categoryMap = response.body().jsonPath().getMap("result", Long.class, List.class);
    assertThat(categoryMap.size()).isEqualTo(3);
  }

  @Test
  @DisplayName("모든 카테고리를 조회합니다.(두번째 결과는 캐싱된 값을 반환합니다.)")
  void getAllCategories_cache_success() {
    Category houseItem = saveCategory("생활용품", null);
    Category toiletPaper = saveCategory("화장지", houseItem);
    Category wetWipe = saveCategory("물티슈", toiletPaper);
    Category paperTowel = saveCategory("키친타올", toiletPaper);

    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .when().get("/categories/hierarchy")
        .then().log().all()
        .extract();


    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<HierarchyCategoryDto> categories = response.body().jsonPath().getList("result", HierarchyCategoryDto.class);
    assertThat(categories).hasSize(1);
    assertThat(categories.get(0).getName()).isEqualTo("생활용품");
    assertThat(categories.get(0).getSubCategories().get(0).getName()).isEqualTo("화장지");
    assertThat(categories.get(0).getSubCategories().get(0).getSubCategories().get(0).getName()).isEqualTo("물티슈");
    assertThat(categories.get(0).getSubCategories().get(0).getSubCategories().get(1).getName()).isEqualTo("키친타올");
  }

  @Test
  @DisplayName("한단계 아래의 서브 카테고리를 조회합니다.")
  void getOneStepSubCategories_success() {
    Category food = categoryRepository.saveAndFlush(CategoryFixture.FOOD.toEntity());
    Category fruit = categoryRepository.saveAndFlush(CategoryFixture.FRUIT.toEntity(food));
    Category meet = categoryRepository.saveAndFlush(CategoryFixture.MEET.toEntity(food));
    Category appleAndPear = categoryRepository.saveAndFlush(CategoryFixture.APPLE_PEAR.toEntity(fruit));

    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .pathParam("id", food.getId())
        .when().get("/categories/{id}/subCategories")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<SubCategoryDto> subCategories = response.body().jsonPath().getList("result", SubCategoryDto.class);
      assertThat(subCategories.get(0).getId()).isEqualTo(fruit.getId());
    assertThat(subCategories.get(0).getName()).isEqualTo(fruit.getName());
    assertThat(subCategories.get(0).getSubCategoryExists()).isTrue();
    assertThat(subCategories.get(1).getId()).isEqualTo(meet.getId());
    assertThat(subCategories.get(1).getName()).isEqualTo(meet.getName());
    assertThat(subCategories.get(1).getSubCategoryExists()).isFalse();
  }

  @Test
  @DisplayName("카테고리에 해당하는 물품을 조회합니다.")
  void getProductsInCategory_success() {
    productFactory.createProduct(ProductFixture.APPLE, seller);
    productFactory.createProduct(ProductFixture.BEEF, seller);
    Long categoryId = CategoryFixture.FOOD.getId();

    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .pathParam("categoryId", categoryId)
        .when().get("/categories/{categoryId}/products")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<ProductSimpleDto> productList = response.jsonPath().getList("result.content", ProductSimpleDto.class);
    assertThat(productList).extracting("id").contains(ProductFixture.BEEF.getId());
    assertThat(productList).extracting("id").contains(ProductFixture.APPLE.getId());
  }

  @Test
  @DisplayName("카테고리에 해당하는 물품을 조회합니다. (캐시에서 조회합니다)")
  void getProductsInCategory_fromCache_success() {
    productFactory.createProduct(ProductFixture.APPLE, seller);
    productFactory.createProduct(ProductFixture.BEEF, seller);
    Long categoryId = CategoryFixture.FOOD.getId();
    // 전체 카테고리 조회 호출하여 전체 카테고리 캐시 시키기
    RestAssured.given().log().all()
        .when().get("/categories")
        .then().log().all()
        .extract();


    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .pathParam("categoryId", categoryId)
        .when().get("/categories/{categoryId}/products")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<ProductSimpleDto> productList = response.jsonPath().getList("result.content", ProductSimpleDto.class);
    assertThat(productList).extracting("id").contains(ProductFixture.BEEF.getId());
    assertThat(productList).extracting("id").contains(ProductFixture.APPLE.getId());
  }

  @Test
  @DisplayName("상품 상세 조회에 성공합니다.")
  void getProduct_success() {
    // given
    Product product = productFactory.createProduct(ProductFixture.BEEF, seller);
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .pathParam("id", product.getId())
        .when().get("/products/{id}")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    ProductDetailDto productDetailDto= response.body().jsonPath().getObject("result", ProductDetailDto.class);
    assertThat(productDetailDto.getId()).isEqualTo(product.getId());
    assertThat(productDetailDto.getSellerUsername()).isEqualTo(seller.getUsername());
    assertThat(productDetailDto.getProductName()).isEqualTo(product.getName());
    assertThat(productDetailDto.getPrice()).isEqualTo(product.getPrice());
    assertThat(productDetailDto.getImages()).satisfiesAnyOf(
        images -> assertThat(images).isNull(),
        images -> assertThat(images).hasSize(product.getProductImages().size())
    );
    assertThat(productDetailDto.getDetail()).endsWith(product.getDetail());
  }

  @Test
  @DisplayName("상품ID에 해당하는 상품이 존재하지 않아 상폼 상세 조회에 실패합니다.")
  void getProduct_fail_because_product_not_exists() {
    // given
    Long notExistsId = 9999L;
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .pathParam("id", notExistsId)
        .when().get("/products/{id}")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }
}
