package com.kyumall.kyumallcommon.product.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter @NoArgsConstructor
public class UpdateCommentRequest {
  private String comment;
}