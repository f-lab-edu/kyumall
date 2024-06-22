package com.kyumall.kyumallclient.order;

import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.order.OrderService;
import com.kyumall.kyumallcommon.order.dto.CreateOrderRequest;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/orderGroups")
@RestController
public class OrderController {
  private final OrderService orderService;

  @PostMapping()
  public ResponseWrapper<CreatedIdDto> createOrderGroup(@LoginUser AuthenticatedUser authenticatedUser,
                          @RequestBody CreateOrderRequest request) {
    return ResponseWrapper.ok(
        CreatedIdDto.of(orderService.createOrderGroup(authenticatedUser.getMemberId(), request)));
  }

  @PostMapping("/{id}/pay")
  public void payOrder(@PathVariable Long id, @LoginUser AuthenticatedUser authenticatedUser) {
    orderService.payOrder(id, authenticatedUser.getMemberId());
  }
}
