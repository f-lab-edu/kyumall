package com.kyumall.kyumallclient.order;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor @Builder
public class PayOrderRequest {
  private List<Long> productIds;
}
