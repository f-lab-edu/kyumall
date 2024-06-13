package com.kyumall.kyumallclient.product.product.dto;

import com.kyumall.kyumallcommon.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductDetailDto {
  private Long id;
  private String sellerUsername;
  private String productName;
  private Integer price;
  private String image;
  private String detail;

  public static ProductDetailDto from(Product product) {
    return ProductDetailDto.builder()
        .id(product.getId())
        .sellerUsername(product.getSeller().getUsername())
        .productName(product.getName())
        .price(product.getPrice())
        .image(product.getImage())
        .detail(product.getDetail())
        .build();
  }
}
