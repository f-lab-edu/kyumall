package com.kyumall.kyumallclient.order;

import com.kyumall.kyumallcommon.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderService {
  private final OrderRepository orderRepository;

  public void createOrder() {
    // 결재전
  }

  public void payOrder() {
    // order 안의 상품 조회

    // 재고 체크

    // 결재

    // 재고 감소

    // 결재완료로 상태 변경
  }
}
