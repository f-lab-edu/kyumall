package com.kyumall.kyumallcommon.product.product.dto;

import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.ProductStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductSearchDto {
  private Long id;
  private String productName;
  private Long sellerId;
  private String sellerName;
  private ProductStatus productStatus;
  private Integer price;

  @Builder
  @QueryProjection
  public ProductSearchDto(Long id, String productName, Long sellerId, String sellerName,
      ProductStatus productStatus, Integer price) {
    this.id = id;
    this.productName = productName;
    this.sellerId = sellerId;
    this.sellerName = sellerName;
    this.productStatus = productStatus;
    this.price = price;
  }
}
