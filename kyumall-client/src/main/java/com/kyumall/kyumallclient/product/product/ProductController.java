package com.kyumall.kyumallclient.product.product;

import com.kyumall.kyumallcommon.product.product.StockService;
import com.kyumall.kyumallcommon.product.product.ProductService;
import com.kyumall.kyumallcommon.product.product.dto.CreateProductRequest;
import com.kyumall.kyumallclient.product.product.dto.CreateProductResponse;
import com.kyumall.kyumallcommon.product.product.dto.ProductDetailDto;
import com.kyumall.kyumallcommon.product.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {
  private final ProductService productService;
  private final StockService stockService;

  /**
   * 상품 생성
   * @TODO: Admin 으로 이동 예정
   * @param request
   * @return
   */
  @PostMapping
  public ResponseWrapper<CreateProductResponse> createProduct(@RequestBody CreateProductRequest request) {
    Long productId = productService.createProduct(request);
    return ResponseWrapper.ok(new CreateProductResponse(productId));
  }

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

  /**
   * id에 해당하는 상품의 재고 변경
   * @TODO: Admin 으로 이동 예정
   * @param id
   * @param authenticatedUser
   * @param quantity
   * @return
   */
  @PutMapping("/{id}/change-stock")
  public ResponseWrapper<Void> changeInventory(@PathVariable Long id,
                                              @LoginUser AuthenticatedUser authenticatedUser,
                                              @RequestParam Long quantity) {
    stockService.updateStock(id, authenticatedUser.getMemberId(), quantity);
    return ResponseWrapper.ok();
  }
}
