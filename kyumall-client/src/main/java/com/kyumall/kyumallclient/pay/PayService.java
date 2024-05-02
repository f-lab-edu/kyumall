package com.kyumall.kyumallclient.pay;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PayService {
  private final PayOpenFeign payOpenFeign;

  public void pay(Long memberId, Long payAmount, String mockResult) {
    payOpenFeign.pay(memberId, payAmount, mockResult);
  }
}
