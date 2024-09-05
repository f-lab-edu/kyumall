package com.kyumall.kyumalladmin.integration;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryMapService;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.category.dto.CategoryDto;
import com.kyumall.kyumallcommon.product.category.dto.CreateCategoryRequest;
import com.kyumall.kyumallcommon.product.category.dto.UpdateCategoryRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@DisplayName("카테고리 ADMIN 기능 통합테스트")
class CategoryAdminIntegrationTest extends IntegrationTest {
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private CategoryMapService categoryMapService;

  @Test
  @DisplayName("최상위 카테고리 생성에 성공합니다.")
  void addCategory_top_category_success() {
    CreateCategoryRequest request = CreateCategoryRequest.builder()
        .name(CategoryFixture.FOOD.getName())
        .parentId(0L)
        .build();

    ExtractableResponse<Response> response = requestCreateCategory(request);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    Long createdId = response.body().jsonPath().getLong("result.id");
    Category newCategory = categoryRepository.findById(createdId).orElseThrow();
    assertThat(newCategory.getName()).isEqualTo(request.getName());
    assertThat(newCategory.getParent()).isNull();
  }

  private static ExtractableResponse<Response> requestCreateCategory(CreateCategoryRequest request) {
    return RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/categories")
        .then().log().all()
        .extract();
  }

  private static ExtractableResponse<Response> requestUpdateCategory(Long id, UpdateCategoryRequest request) {
    return RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .pathParam("id", id)
        .when().put("/categories/{id}")
        .then().log().all()
        .extract();
  }

  @Test
  @DisplayName("하위 카테고리 생성에 성공합니다. 음식 카테고리 하위에 사과,배 카테고리를 추가합니다.")
  void addCategory_subcategory_success() {
    // 상위 카테고리
    Category parentCategory = productFactory.createCategory(CategoryFixture.FOOD);
    // 하위 카테고리
    CreateCategoryRequest request = CreateCategoryRequest.builder()
        .name(CategoryFixture.APPLE_PEAR.getName())
        .parentId(parentCategory.getId())
        .build();

    ExtractableResponse<Response> response = requestCreateCategory(request);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    Long createdId = response.body().jsonPath().getLong("result.id");
    Category newCategory = categoryRepository.findById(createdId).orElseThrow();
    assertThat(newCategory.getName()).isEqualTo(request.getName());
    assertThat(newCategory.getParent().getId()).isEqualTo(parentCategory.getId());
  }

  @Test
  @DisplayName("카테고리 생성 요청 성공시, 카테고리 캐시가 만료됩니다.")
  void addCategory_cacheEvict_success() {
    // FOOD 카테고리 추가
    Category parentCategory = productFactory.createCategory(CategoryFixture.FOOD);
    // 기존데이터 캐시
    Map<String, List<CategoryDto>> cachedCategoryBeforeAdd = categoryMapService.findCategoryGroupingByParent();

    // when 사과,배 카테고리 추가 및 캐시 만료
    CreateCategoryRequest request = CreateCategoryRequest.builder()
        .name(CategoryFixture.APPLE_PEAR.getName())
        .parentId(parentCategory.getId())
        .build();
    ExtractableResponse<Response> response = requestCreateCategory(request);

    // then
    // 새 데이터 캐시
    Map<String, List<CategoryDto>> cachedCategoryAfterAdd = categoryMapService.findCategoryGroupingByParent();
    assertThat(cachedCategoryBeforeAdd).hasSize(1);
    assertThat(cachedCategoryAfterAdd).hasSize(2);  // 캐시 만료후 새 데이터 캐시됨
  }

  @Test
  @DisplayName("카테고리 수정(부모 변경)에 성공합니다. Fruit 부모를 Food에서 Meet로 변경합니다.")
  void updateCategory_change_parent_success() {
    // 상위 카테고리
    Category fruit  = productFactory.createCategory(CategoryFixture.FRUIT);
    Category meet  = productFactory.createCategory(CategoryFixture.MEET);

    // 하위 카테고리
    UpdateCategoryRequest request = UpdateCategoryRequest.builder()
        .newName(fruit.getName())
        .newParentId(meet.getParentId())
        .build();

    ExtractableResponse<Response> response = requestUpdateCategory(fruit.getId(), request);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    Category newCategory = categoryRepository.findById(fruit.getId()).orElseThrow();
    assertThat(newCategory.getName()).isEqualTo(request.getNewName());
    assertThat(newCategory.getParent().getId()).isEqualTo(request.getNewParentId());
  }

}
