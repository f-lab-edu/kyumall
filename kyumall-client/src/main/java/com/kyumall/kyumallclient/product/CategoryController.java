package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.CategoryDto;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
import com.kyumall.kyumallclient.product.dto.SubCategoryDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import java.util.List;
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
  private final CategoryService categoryService;

  /**
   * 전체 카테고리를 조회합니다.
   * @return
   */
  @GetMapping
  public ResponseWrapper<List<CategoryDto>> getAllCategories() {
    return ResponseWrapper.ok(categoryService.getAllCategories());
  }

  /**
   * 카테고리 ID의 한단계 아래 서브 카테고리 목록을를 조회합니다.
   * 한단계 아래의 서브 카테고리만 조회합니다.
   * @return
   */
  @GetMapping("/{id}/subCategories")
  public ResponseWrapper<List<SubCategoryDto>> getOneStepSubCategories(@PathVariable Long id) {
    return ResponseWrapper.ok(categoryService.getOneStepSubCategories(id));
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
