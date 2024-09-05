package com.kyumall.kyumalladmin.category;

import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryMapService;
import com.kyumall.kyumallcommon.product.category.CategoryService;
import com.kyumall.kyumallcommon.product.category.dto.CreateCategoryRequest;
import com.kyumall.kyumallcommon.product.category.dto.UpdateCategoryRequest;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    categoryMapService.evictCategoryMapCache();
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
    categoryMapService.evictCategoryMapCache();
  }

}
