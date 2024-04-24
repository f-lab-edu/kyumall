package com.kyumall.kyumallclient.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PayServiceImpl implements PayService {

  public boolean pay(Long memberId, Long payAmount) {
    return true;
  }
}
