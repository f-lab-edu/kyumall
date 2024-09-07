package com.kyumall.kyumallclient.product.category.dto;

import com.kyumall.kyumallcommon.product.category.dto.CategoryDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class HierarchyCategoryDto {
  private Long id;
  private String name;
  private List<HierarchyCategoryDto> subCategories = new ArrayList<>();

  public HierarchyCategoryDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public static HierarchyCategoryDto from(CategoryDto categoryDto) {
    return new HierarchyCategoryDto(Long.parseLong(categoryDto.getId()), categoryDto.getName());
  }

  public void setSubCategories(List<HierarchyCategoryDto> subCategories) {
    this.subCategories = subCategories;
  }
}
