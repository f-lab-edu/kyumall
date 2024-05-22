package com.kyumall.kyumallclient.pay;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PayService {
  private final PayOpenFeign payOpenFeign;

  public boolean pay(Long memberId, Long payAmount) {
    PayResponse payResponse = payOpenFeign.pay(memberId, payAmount);
    return true;
  }
}