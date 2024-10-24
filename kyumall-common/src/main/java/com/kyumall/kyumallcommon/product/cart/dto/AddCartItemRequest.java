package com.kyumall.kyumallcommon.product.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class AddCartItemRequest {
  private Long productId;
  private Integer count;
}
