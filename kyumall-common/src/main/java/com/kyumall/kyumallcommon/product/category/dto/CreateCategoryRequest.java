package com.kyumall.kyumallcommon.product.category.dto;

import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryStatus;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class CreateCategoryRequest {
  private String name;
  @Nullable
  private Long parentId;

  public Category toEntity(Category parent) {
    return Category.builder()
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build();
  }
}
