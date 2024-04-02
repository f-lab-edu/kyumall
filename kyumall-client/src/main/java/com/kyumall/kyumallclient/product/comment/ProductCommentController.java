package com.kyumall.kyumallclient.product.comment;

import com.kyumall.kyumallclient.product.comment.dto.CreateCommentRequest;
import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/products/{id}/comments")
@RestController
public class ProductCommentController {
  private final ProductCommentService productCommentService;

  @PostMapping
  public ResponseWrapper<CreatedIdDto> createComment(
                                          @PathVariable Long id,
                                          @LoginUser AuthenticatedUser authenticatedUser,
                                          @Validated @RequestBody CreateCommentRequest createCommentRequest) {
    return ResponseWrapper.ok(CreatedIdDto.of(
        productCommentService.createComment(id, authenticatedUser.getMemberId(), createCommentRequest)));
  }
}
