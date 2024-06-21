package com.kyumall.kyumallcommon.pay;

import com.kyumall.kyumallcommon.pay.PayOpenFeign;
import com.kyumall.kyumallcommon.pay.PayResponse;
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
