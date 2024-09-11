package com.kyumall.kyumallcommon.product.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor @Builder @NoArgsConstructor
@Getter
public class CreateCommentRequest {
  @NotEmpty
  private String comment;
}
