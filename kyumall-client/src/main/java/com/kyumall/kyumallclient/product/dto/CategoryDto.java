package com.kyumall.kyumallclient.product.dto;

import com.kyumall.kyumallcommon.product.entity.Category;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CategoryDto {
  private Long id;
  private String name;
  private List<CategoryDto> subCategories = new ArrayList<>();
  public CategoryDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
  public static CategoryDto from(Category category) {
    return new CategoryDto(category.getId(), category.getName());
  }
  public void setSubCategories(List<CategoryDto> subCategories) {
    this.subCategories = subCategories;
  }
}
