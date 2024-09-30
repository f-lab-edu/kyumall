package com.kyumall.kyumalladmin.integration;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumalladmin.AuthTestUtil;
import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
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
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@DisplayName("카테고리 ADMIN 기능 통합테스트")
class CategoryAdminIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private CategoryMapService categoryMapService;

  Member adminMike;

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    adminMike = memberFactory.createMember(MemberFixture.MIKE);
  }

  @Test
  @DisplayName("최상위 카테고리 생성에 성공합니다.")
  void addCategory_top_category_success() {
    CreateCategoryRequest request = CreateCategoryRequest.builder()
        .name(CategoryFixture.FOOD.getName())
        .parentId(0L)
        .build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    ExtractableResponse<Response> response = requestCreateCategory(request, spec);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    Long createdId = response.body().jsonPath().getLong("result.id");
    Category newCategory = categoryRepository.findById(createdId).orElseThrow();
    assertThat(newCategory.getName()).isEqualTo(request.getName());
    assertThat(newCategory.getParent()).isNull();
  }

  private static ExtractableResponse<Response> requestCreateCategory(CreateCategoryRequest request,
      RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/categories")
        .then().log().all()
        .extract();
  }

  private static ExtractableResponse<Response> requestUpdateCategory(Long id, UpdateCategoryRequest request,
      RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .pathParam("id", id)
        .when().put("/categories/{id}")
        .then().log().all()
        .extract();
  }

  private static ExtractableResponse<Response> requestApplyCategoryToClientApp(
      RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .when().post("/categories/apply-to-client-app")
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
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    ExtractableResponse<Response> response = requestCreateCategory(request, spec);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    Long createdId = response.body().jsonPath().getLong("result.id");
    Category newCategory = categoryRepository.findById(createdId).orElseThrow();
    assertThat(newCategory.getName()).isEqualTo(request.getName());
    assertThat(newCategory.getParent().getId()).isEqualTo(parentCategory.getId());
  }

  @Test
  @DisplayName("카테고리 생성 요청을 성공하더라도, 캐시가 만료되지 않습니다.")
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
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    ExtractableResponse<Response> response = requestCreateCategory(request, spec);

    // then
    // 새 데이터 캐시
    Map<String, List<CategoryDto>> cachedCategoryAfterAdd = categoryMapService.findCategoryGroupingByParent();
    assertThat(cachedCategoryBeforeAdd).hasSize(1);
    assertThat(cachedCategoryAfterAdd).hasSize(1);  // 수정후에도, 캐시에는 기존의 1건만 존재
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
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    ExtractableResponse<Response> response = requestUpdateCategory(fruit.getId(), request, spec);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    Category newCategory = categoryRepository.findById(fruit.getId()).orElseThrow();
    assertThat(newCategory.getName()).isEqualTo(request.getNewName());
    assertThat(newCategory.getParent().getId()).isEqualTo(request.getNewParentId());
  }

  @Test
  @DisplayName("카테고리 만료 기능 호출 시, 기존 카테고리가 만료되고, 신규 카테고리가 캐시됩니다")
  void applyCategoryToClientApp_category_evict_success() {
    // 캐시된 카테고리 조회
    Category food = productFactory.createCategory(CategoryFixture.FOOD);
    List<CategoryDto> categoryMapBeforeAdd = categoryMapService.findCategoryGroupingByParent().get("0");
    // 캐시 추가
    Category fashion = productFactory.createCategory(CategoryFixture.FASHION);
    List<CategoryDto> categoryMapAfterAdd = categoryMapService.findCategoryGroupingByParent().get("0");
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    // when
    // 캐시 만료
    requestApplyCategoryToClientApp(spec);

    // then
    // 캐시된 카테고리 조회
    List<CategoryDto> categoryMapAfterEvict = categoryMapService.findCategoryGroupingByParent().get("0");
    assertThat(categoryMapBeforeAdd).hasSize(1);
    assertThat(categoryMapAfterAdd).hasSize(1);
    assertThat(categoryMapAfterEvict).hasSize(2); // 캐시 만료 후 2개로 증가
  }
}
