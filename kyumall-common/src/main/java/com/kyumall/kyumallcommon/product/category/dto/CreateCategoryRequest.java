package com.kyumall.kyumallcommon.product.category.dto;

import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class CreateCategoryRequest {
  @NotEmpty
  private String name;
  @NotNull @Min(0)
  private Long parentId;  // 0일 경우, 최상위 카테고리

  public Category toEntity(Category parent) {
    return Category.builder()
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build();
  }
}
