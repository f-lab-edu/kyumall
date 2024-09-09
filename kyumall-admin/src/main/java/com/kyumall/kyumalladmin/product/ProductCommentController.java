package com.kyumall.kyumalladmin.product;

import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.product.product.ProductCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/products/{productId}/comments")
@RestController
public class ProductCommentController {
  private final ProductCommentService productCommentService;

  @DeleteMapping("/{id}")
  public void deleteCommentByAdmin(@PathVariable Long productId, @PathVariable Long id,
      @LoginUser AuthenticatedUser loginUser) {
    productCommentService.deleteCommentByAdmin(productId, id, loginUser.getMemberId());
  }
}
