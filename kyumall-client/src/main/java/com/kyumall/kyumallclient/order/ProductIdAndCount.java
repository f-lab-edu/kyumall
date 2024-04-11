package com.kyumall.kyumallclient.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class ProductIdAndCount {
  private Long productId;
  private Integer count;
}
