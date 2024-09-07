package com.kyumall.kyumalladmin.product;

import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.product.product.ProductService;
import com.kyumall.kyumallcommon.product.product.StockService;
import com.kyumall.kyumallcommon.product.product.dto.CreateProductRequest;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/products")
@RequiredArgsConstructor
@RestController
public class ProductController {
  private final ProductService productService;
  private final StockService stockService;

  /**
   * 상품 생성
   * @param request
   * @return
   */
  @PostMapping
  public ResponseWrapper<CreatedIdDto> createProduct(@RequestBody CreateProductRequest request) {
    Long productId = productService.createProduct(request);
    return ResponseWrapper.ok(new CreatedIdDto(productId));
  }

  /**
   * id에 해당하는 상품의 재고 변경
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
