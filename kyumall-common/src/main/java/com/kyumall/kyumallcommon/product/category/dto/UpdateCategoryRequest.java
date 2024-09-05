package com.kyumall.kyumallcommon.product.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class UpdateCategoryRequest {
  @NotEmpty
  private String newName;
  @NotNull @Min(0)
  private Long newParentId; // 0일 경우, 최상위 카테고리
}
