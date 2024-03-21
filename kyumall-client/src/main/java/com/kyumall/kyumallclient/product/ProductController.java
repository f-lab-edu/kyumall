package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.CreateProductRequest;
import com.kyumall.kyumallclient.product.dto.CreateProductResponse;
import com.kyumall.kyumallclient.product.dto.ProductDetailDto;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {
  private final ProductService productService;

  @PostMapping
  public ResponseWrapper<CreateProductResponse> createProduct(@RequestBody CreateProductRequest request) {
    Long productId = productService.createProduct(request);
    return ResponseWrapper.ok(new CreateProductResponse(productId));
  }

  @GetMapping
  public ResponseWrapper<Page<ProductSimpleDto>> getAllProducts(@PageableDefault(size = 10) Pageable pageable) {
    return ResponseWrapper.ok(productService.getAllProducts(pageable));
  }

  @GetMapping("/{id}")
  public ResponseWrapper<ProductDetailDto> getProduct(@PathVariable Long id) {
    return ResponseWrapper.ok(productService.getProduct(id));
  }
}
