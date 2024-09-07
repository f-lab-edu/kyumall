package com.kyumall.kyumallcommon.product.product.dto;

import com.kyumall.kyumallcommon.product.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductSimpleDto {
  private Long id;
  private String name;
  private Integer price;
  private String image;

  public static ProductSimpleDto from(Product product) {
    return ProductSimpleDto.builder()
        .id(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .image(product.getImage())
        .build();
  }
}
