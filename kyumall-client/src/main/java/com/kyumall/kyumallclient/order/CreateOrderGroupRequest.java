package com.kyumall.kyumallclient.order;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter @NoArgsConstructor @AllArgsConstructor
public class CreateOrderGroupRequest {
  private List<ProductIdAndCount> productIdAndCounts;
}
