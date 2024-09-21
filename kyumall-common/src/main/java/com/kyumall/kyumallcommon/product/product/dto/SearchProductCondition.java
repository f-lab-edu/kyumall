package com.kyumall.kyumallcommon.product.product.dto;

import com.kyumall.kyumallcommon.product.product.entity.ProductStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class SearchProductCondition {
  private String name;
  private Long sellerId;
  private List<ProductStatus> productStatusList;
  private Integer maxPrice;
  private Integer minPrice;
}
