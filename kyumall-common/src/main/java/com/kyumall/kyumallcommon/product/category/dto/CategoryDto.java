package com.kyumall.kyumallcommon.product.category.dto;

import com.kyumall.kyumallcommon.product.category.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 카테고리 Dto
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Getter
public class CategoryDto {
  private String id;
  private String name;
  private String parentId;

  public static CategoryDto from(Category category) {
    return CategoryDto.builder()
        .id(category.getId().toString())
        .name(category.getName())
        .parentId(category.getParentId().toString())
        .build();
  }
}
