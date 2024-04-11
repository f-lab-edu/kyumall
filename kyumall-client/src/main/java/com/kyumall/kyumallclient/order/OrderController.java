package com.kyumall.kyumallclient.order;

import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/orders")
@RestController
public class OrderController {
  private final OrderService orderService;

  @PostMapping()
  public ResponseWrapper<CreatedIdDto> createOrder(@LoginUser AuthenticatedUser authenticatedUser,
                          @RequestBody CreateOrderRequest request) {
    return ResponseWrapper.ok(
        CreatedIdDto.of(orderService.createOrder(authenticatedUser.getMemberId(), request)));
  }
}
