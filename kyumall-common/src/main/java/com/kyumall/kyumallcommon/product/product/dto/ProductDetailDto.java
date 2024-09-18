package com.kyumall.kyumallcommon.product.product.dto;

import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.ProductImage;
import java.util.List;
import java.util.stream.Collectors;
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
  private List<String> images;
  private String detail;

  public static ProductDetailDto from(Product product) {
    return ProductDetailDto.builder()
        .id(product.getId())
        .sellerUsername(product.getSeller().getUsername())
        .productName(product.getName())
        .price(product.getPrice())
        .images(extractImageIdList(product))
        .detail(product.getDetail())
        .build();
  }

  private static List<String> extractImageIdList(Product product) {
    if (product.getProductImages() == null) {
      return null;
    }
    return product.getProductImages().stream()
        .map(ProductImage::getImageId)
        .collect(Collectors.toList());
  }
}
