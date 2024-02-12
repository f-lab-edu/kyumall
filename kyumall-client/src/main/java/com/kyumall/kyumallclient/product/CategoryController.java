package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.CategoryDto;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
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

// TODO: 캐시 , 잘 바뀌지 않는 데이터, map or class
// TODO: 리로드 API
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
    return ResponseWrapper.ok(productService.getAllCategories());
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
