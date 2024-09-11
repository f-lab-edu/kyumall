package com.kyumall.kyumallclient.product.product;

import com.kyumall.kyumallcommon.product.product.ProductService;
import com.kyumall.kyumallcommon.product.product.dto.ProductDetailDto;
import com.kyumall.kyumallcommon.product.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {
  private final ProductService productService;

  /**
   * 전체 상품 조회
   * @param pageable
   * @return
   */
  @GetMapping
  public ResponseWrapper<Page<ProductSimpleDto>> getAllProducts(@PageableDefault(size = 10) Pageable pageable) {
    return ResponseWrapper.ok(productService.getAllProducts(pageable));
  }

  /**
   * id로 상품 조회
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  public ResponseWrapper<ProductDetailDto> getProduct(@PathVariable Long id) {
    return ResponseWrapper.ok(productService.getProduct(id));
  }
}
