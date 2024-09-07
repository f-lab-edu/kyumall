package com.kyumall.kyumallcommon.product.product.dto;

import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class ProductCommentDto {
  private Long id;
  private String username;  // 작성자 아이디
  private String email;   // 작성자 이메일
  private String comment;
  private LocalDateTime createdAt;
  private Long replyCount;  // 대댓글 수
  private Long likeCount; // 댓글 좋아요 수
  private Long dislikeCount;  // 댓글 싫어요 수
  private boolean likeByCurrentUser; // 현재 유저가 좋아요 했는지 여부
  private boolean dislikeByCurrentUser; // 현재 유저가 싫어요 했는지 여부


  public static ProductCommentDto from(ProductComment productComment) {
    return ProductCommentDto.builder()
        .id(productComment.getId())
        .username(productComment.getMember().getUsername())
        .email(productComment.getMember().getEmail())
        .comment(productComment.getContent())
        .createdAt(productComment.getCreatedAt())
        .build();
  }

  public void setRatingCount(LikeCountDto countDto) {
    this.likeCount = countDto.getLikeCount();
    this.dislikeCount = countDto.getDislikeCount();
    this.likeByCurrentUser = countDto.isLikeByCurrentUser();
    this.dislikeByCurrentUser = countDto.isDislikeByCurrentUser();
  }

  public void setReplyCount(Long replyCount) {
    this.replyCount = replyCount;
  }
}
