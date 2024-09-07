package com.kyumall.kyumallclient.product.product;

import com.kyumall.kyumallcommon.product.product.ProductCommentService;
import com.kyumall.kyumallcommon.product.product.dto.CreateCommentRequest;
import com.kyumall.kyumallcommon.product.product.dto.ProductCommentDto;
import com.kyumall.kyumallcommon.product.product.dto.UpdateCommentRequest;
import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.product.product.entity.RatingType;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/products/{id}/comments")
@RestController
public class ProductCommentController {
  private final ProductCommentService productCommentService;

  /**
   * 상품에 댓글 추가
   * @param id 상품 ID
   * @param authenticatedUser
   * @param createCommentRequest
   * @return
   */
  @PostMapping
  public ResponseWrapper<CreatedIdDto> createComment(
                                          @PathVariable Long id,
                                          @LoginUser AuthenticatedUser authenticatedUser,
                                          @Validated @RequestBody CreateCommentRequest createCommentRequest) {
    return ResponseWrapper.ok(CreatedIdDto.of(
        productCommentService.createComment(id, authenticatedUser.getMemberId(), createCommentRequest)));
  }

  /**
   * 상품 ID로 최상위 댓글 조회
   * @param id 상품 ID
   * @param pageable page
   * @param authenticatedUser
   * @return
   */
  @GetMapping
  public ResponseWrapper<Slice<ProductCommentDto>> getComments(@PathVariable Long id,
      @PageableDefault(size = 10) Pageable pageable, @LoginUser AuthenticatedUser authenticatedUser) {
    return ResponseWrapper.ok(productCommentService.getComments(id, pageable, authenticatedUser));
  }

  /**
   * 댓글 수정
   * @param id 상품 ID
   * @param commentId
   * @param authenticatedUser
   * @param request
   * @return
   */
  @PutMapping("/{commentId}")
  public ResponseWrapper<Void> updateComment(@PathVariable Long id,
      @PathVariable Long commentId, @LoginUser AuthenticatedUser authenticatedUser,
      @RequestBody UpdateCommentRequest request) {
    productCommentService.updateComment(id, commentId, authenticatedUser.getMemberId(), request);
    return ResponseWrapper.ok();
  }

  /**
   * 댓글 삭제
   * @param id 상품 ID
   * @param commentId
   * @param authenticatedUser
   * @return
   */
  @DeleteMapping("/{commentId}")
  public ResponseWrapper<Void> deleteComment(@PathVariable Long id,
      @PathVariable Long commentId, @LoginUser AuthenticatedUser authenticatedUser) {
    productCommentService.deleteComment(id, commentId, authenticatedUser.getMemberId());
    return ResponseWrapper.ok();
  }

  /**
   * 댓글 좋아요/싫어요
   * @param id 상품 ID
   * @param commentId
   * @param authenticatedUser
   * @param ratingType
   * @return
   */
  @PutMapping("/{commentId}/update-rating")
  public ResponseWrapper<Void> updateCommentRating(@PathVariable Long id, @PathVariable Long commentId,
      @LoginUser AuthenticatedUser authenticatedUser, @RequestParam RatingType ratingType) {
    productCommentService.updateCommentRating(id, commentId, authenticatedUser.getMemberId(), ratingType);
    return ResponseWrapper.ok();
  }

  /**
   * 대댓글 추가
   * @param id 상품 ID
   * @param commentId
   * @param authenticatedUser
   * @param createCommentRequest
   * @return
   */
  @PostMapping("/{commentId}/reply")
  public ResponseWrapper<CreatedIdDto> createCommentReply(
                            @PathVariable Long id,
                            @PathVariable Long commentId,
                            @LoginUser AuthenticatedUser authenticatedUser,
                            @Validated @RequestBody CreateCommentRequest createCommentRequest) {
    return ResponseWrapper.ok(CreatedIdDto.of(
        productCommentService.createCommentReply(id, commentId ,authenticatedUser.getMemberId(), createCommentRequest)));
  }

  /**
   * 대댓글 조회
   * @param id
   * @param commentId
   * @param authenticatedUser
   * @param pageable
   * @return
   */
  @GetMapping("/{commentId}/reply")
  public ResponseWrapper<Slice<ProductCommentDto>> getCommentReplies(
                        @PathVariable Long id,
                        @PathVariable Long commentId,
                        @LoginUser AuthenticatedUser authenticatedUser,
                        @PageableDefault(size = 10) Pageable pageable) {
    return ResponseWrapper.ok(productCommentService.getCommentReplies(id, commentId, authenticatedUser, pageable));
  }

}
