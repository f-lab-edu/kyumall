package com.kyumall.kyumallclient.product.dto;

import com.kyumall.kyumallcommon.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductSimpleDto {
  private String name;
  private Integer price;
  private String image;

  public static ProductSimpleDto from(Product product) {
    return ProductSimpleDto.builder()
        .name(product.getName())
        .price(product.getPrice())
        .image(product.getImage().getImageUrl())
        .build();
  }
}
