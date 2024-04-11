package com.kyumall.kyumallclient.order;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/orders")
@RestController
public class OrderController {
  private final OrderService orderService;

  @PostMapping("/")
  public void createOrder() {
    orderService.createOrder();
  }

  @PostMapping("/{id}/pay")
  public void payOrder() {
    orderService.payOrder();
  }
}
