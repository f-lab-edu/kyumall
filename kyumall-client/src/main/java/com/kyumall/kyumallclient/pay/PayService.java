package com.kyumall.kyumallclient.pay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PayService {
  private final PayOpenFeign payOpenFeign;

  public boolean pay(Long memberId, Long payAmount) {
    PayResponse payResponse = payOpenFeign.pay(memberId, payAmount);
    log.info(payResponse.getMessage());
    log.info(payResponse.getResult());
    return true;
  }
}
