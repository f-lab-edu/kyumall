package com.kyumall.kyumallclient.product.category;

import com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto;
import com.kyumall.kyumallcommon.product.product.ProductService;
import com.kyumall.kyumallclient.product.category.dto.HierarchyCategoryDto;
import com.kyumall.kyumallcommon.product.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import io.micrometer.core.annotation.Timed;
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

@Timed("kyumall.category")
@RequiredArgsConstructor
@RequestMapping("/categories")
@RestController
public class CategoryController {
  private final ProductService productService;
  private final CategoryFacade categoryFacade;

  /**
   * 전체 카테고리를 조회합니다. (계층형 리스트 형태)
   * @return
   */
  @GetMapping("/hierarchy")
  public ResponseWrapper<List<HierarchyCategoryDto>> getAllCategoriesHierarchy() {
    return ResponseWrapper.ok(categoryFacade.getAllCategoriesHierarchy());
  }

  /**
   * 전체 카테고리를 조회합니다. (맵 형태)
   * @return
   */
  @GetMapping("/map")
  public ResponseWrapper<Map<Long, List<SubCategoryDto>>> getAllCategoriesMap() {
    return ResponseWrapper.ok(categoryFacade.getAllCategoriesMap());
  }

  /**
   * 카테고리 ID의 한단계 아래 서브 카테고리 목록을를 조회합니다.
   * 한단계 아래의 서브 카테고리만 조회합니다.
   * 최상위 카테고리를 검색하는 경우, ID에 0을 넣습니다.
   * @return
   */
  @GetMapping("/{id}/subCategories")
  public ResponseWrapper<List<SubCategoryDto>> getOneStepSubCategories(@PathVariable Long id) {
    return ResponseWrapper.ok(categoryFacade.getOneStepSubCategoriesByQuery(id));
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
