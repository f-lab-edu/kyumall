package com.kyumall.kyumalladmin.category;

import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryMapService;
import com.kyumall.kyumallcommon.product.category.CategoryService;
import com.kyumall.kyumallcommon.product.category.TopCategoryService;
import com.kyumall.kyumallcommon.product.category.dto.CategoryDto;
import com.kyumall.kyumallcommon.product.category.dto.CreateCategoryRequest;
import com.kyumall.kyumallcommon.product.category.dto.UpdateCategoryRequest;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/categories")
@RequiredArgsConstructor
@RestController
public class CategoryController {

  private final CategoryService categoryService;
  private final CategoryMapService categoryMapService;
  private final TopCategoryService topCategoryService;

  /**
   * 새 카테고리를 생성합니다.
   * parentId 가 0 이면 최상위 카테고리를 의미합니다.
   * parentId가 null 일 경우를 최상위 카테고리로 할 수 도 있으나,
   * 입력값이 없는 경우와 혼돈될 수 있으므로, 명시적으로 0을 입력하는 경우에만 최상위 카테고리로 취급합니다.
   * @param request
   * @return
   */
  @PostMapping
  public ResponseWrapper<CreatedIdDto> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    Category category = categoryService.createCategory(request);
    return ResponseWrapper.ok(CreatedIdDto.of(category.getId()));
  }

  /**
   * 카테고리를 수정합니다.
   * @param id
   * @param request 카테고리 내용, 부모 카테고리
   */
  @PutMapping("/{id}")
  public void updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
    categoryService.updateCategory(id, request);
  }

  /**
   * 캐시되지 않은 전체 카테고리 목록을 DB에서 조회하여 반환합니다.
   * 어드민 페이지에서 캐시 만료 시키기 전, DB에 카테고리 데이터의 최신 상태를 확인하기 위해 사용됩니다.
   * @return
   */
  @GetMapping("/not-cached-list")
  public Map<String, List<CategoryDto>> getAllCategoryNotCachedList() {
    return categoryService.findCategoryGroupingByParent();
  }

  /**
   * Client 어플리케이션에 현재 상태의 카테고리를 반영합니다.
   * 캐시에 저장된 카테고리 데이터를 만료 시킨후, 새 데이터로 다시 캐시합니다.
   * Admin 페이지에서 매번 카테고리 추가/수정이 있을때마다 캐시를 만료시키는 것이 아닌, 수정 작업을 완료한후 한번에 캐시를 만료시킵니다.
   * 카테고리 변경을 반영할 시점을 Admin에서 선택할 수 있고, 변경이 끝난 후 한번에 만료가 되어 캐시 Hit율을 높힐 수 있습니다.
   */
  @PostMapping("/apply-to-client-app")
  public void applyCategoryToClientApp() {
    categoryMapService.evictCategoryMapCache();          // 캐시 만료
    categoryMapService.findCategoryGroupingByParent();   // 캐시 warmup
    topCategoryService.evictTopCategory();
    topCategoryService.findTopCategory();
  }
}
