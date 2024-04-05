package com.kyumall.kyumallcommon.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class ReplyCountDto {
  private Long parentCommentId; // 상위 댓글의 ID
  private Long replyCount;

  public static ReplyCountDto createZeroCount() {
    return ReplyCountDto.builder()
        .replyCount(0L)
        .build();
  }
}
