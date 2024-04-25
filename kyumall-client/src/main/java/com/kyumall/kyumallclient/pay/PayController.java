package com.kyumall.kyumallclient.pay;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RequiredArgsConstructor
@RequestMapping("/api/pay")
@RestController
public class PayController {
  private final PayService payService;
  @PostMapping
  public void pay(@RequestParam Long memberId, @RequestParam Long payAmount) {
    payService.pay(1L, 1000L);
  }
}
