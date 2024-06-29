package com.kyumall.kyumallcommon.product.stock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductAndStockDto {
  private Long productId;
  private Long stockId;
  private Long quantity;
}
