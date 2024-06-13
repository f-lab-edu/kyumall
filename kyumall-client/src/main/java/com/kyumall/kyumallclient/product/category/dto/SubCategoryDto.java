package com.kyumall.kyumallclient.product.category.dto;

import com.kyumall.kyumallcommon.product.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class SubCategoryDto {
  private Long id;
  private String name;
  private Boolean subCategoryExists;  // 하위 카테고리가 존재하는지 (true 시, 열림 버튼 활성화)

  public static SubCategoryDto from(Category category, boolean isSubCategoryExists) {
    return SubCategoryDto.builder()
        .id(category.getId())
        .name(category.getName())
        .subCategoryExists(isSubCategoryExists)
        .build();
  }
}
