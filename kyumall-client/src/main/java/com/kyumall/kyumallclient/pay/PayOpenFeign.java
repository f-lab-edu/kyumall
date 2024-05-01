package com.kyumall.kyumallclient.pay;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "PayOpenFeign", url = "localhost:8081/api")
public interface PayOpenFeign {
  @PostMapping("/pay")
  PayResponse pay(@RequestParam Long memberId, @RequestParam Long totalAmount);
}
