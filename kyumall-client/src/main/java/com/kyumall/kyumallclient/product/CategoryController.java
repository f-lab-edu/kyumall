package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.CategoryDto;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/categories")
@RestController
public class CategoryController {
  private final ProductService productService;

  /**
   * 전체 카테고리를 조회합니다.
   * @return
   */
  @GetMapping
  public ResponseWrapper<List<CategoryDto>> getAllCategories() {
    Map<Long, List<Category>> categoryGroupingByParent = productService.findCategoryGroupingByParent();
    return ResponseWrapper.ok(convertToCategoryHierarchy(categoryGroupingByParent));
  }

  /**
   * 카테고리 Map을 계층형 구조의 List로 변경
   * @param groupingByParentId
   * @return
   */
  private List<CategoryDto> convertToCategoryHierarchy(Map<Long, List<Category>> groupingByParentId) {
    List<CategoryDto> rootCategories = groupingByParentId.get(0L).stream().map(CategoryDto::from)
        .toList();
    addSubCategories(rootCategories, groupingByParentId);
    return rootCategories;
  }

  /**
   * 재귀 호출로 카테고리의 서브 카테고리를 추가
   * @param categoryDtos
   * @param groupingByParentId
   */
  private void addSubCategories(List<CategoryDto> categoryDtos, Map<Long, List<Category>> groupingByParentId) {
    categoryDtos.stream().forEach(
        categoryDto -> {
          List<CategoryDto> subCategories = groupingByParentId.getOrDefault(categoryDto.getId(), new ArrayList<>())
              .stream().map(CategoryDto::from).toList();
          categoryDto.setSubCategories(subCategories);
          addSubCategories(subCategories, groupingByParentId);
        }
    );
  }

  /**
   * 카테고리에 해당하는 상품 목록을 조회합니다.
   * 하위 카테고리 상품까지 조회됩니다.
   * @param categoryId
   * @param pageable
   * @return
   */
  @GetMapping("/{categoryId}/products")
  public ResponseWrapper<Slice<ProductSimpleDto>> getProductsInCategory(@PathVariable Long categoryId,
      @PageableDefault(size = 10) Pageable pageable) {
    return ResponseWrapper.ok(productService.getProductsInCategory(categoryId, pageable));
  }
}
