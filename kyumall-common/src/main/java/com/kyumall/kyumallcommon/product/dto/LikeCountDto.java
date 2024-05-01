package com.kyumall.kyumallcommon.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor @Builder @NoArgsConstructor
@Getter @ToString
public class LikeCountDto {
  private Long productCommentId;
  private Long likeCount;   // 좋아요 수
  private Long dislikeCount;    // 싫어요 수
  private boolean likeByCurrentUser;    // 현재 유저가 좋아요 했는지 여부
  private boolean dislikeByCurrentUser;   // 현재 유저가 싫어요 했는지 여부

  // 기본값 0으로 채워서 반환
  public static LikeCountDto createZeroCount() {
    return LikeCountDto.builder()
        .likeCount(0L)
        .dislikeCount(0L)
        .likeByCurrentUser(false)
        .dislikeByCurrentUser(false)
        .build();
  }
}
