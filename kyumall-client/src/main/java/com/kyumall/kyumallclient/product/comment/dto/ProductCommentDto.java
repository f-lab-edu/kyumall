package com.kyumall.kyumallclient.product.comment.dto;

import com.kyumall.kyumallcommon.product.entity.ProductComment;
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
  //TODO: 추천수, 비추천수
  //TODO: 최상위 댓글의 경우 대댓글의 수

  public static ProductCommentDto from(ProductComment productComment) {
    return ProductCommentDto.builder()
        .id(productComment.getId())
        .username(productComment.getMember().getUsername())
        .email(productComment.getMember().getEmail())
        .comment(productComment.getContent())
        .createdAt(productComment.getCreatedAt())
        .build();
  }
}
