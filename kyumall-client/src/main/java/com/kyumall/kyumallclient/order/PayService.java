package com.kyumall.kyumallclient.order;

public interface PayService {
  boolean pay(Long memberId, Long payAmount);
}
