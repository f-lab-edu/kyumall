package com.kyumall.kyumallclient.product.cart;

import com.kyumall.kyumallcommon.product.cart.CartService;
import com.kyumall.kyumallcommon.product.cart.dto.AddCartItemRequest;
import com.kyumall.kyumallcommon.product.cart.dto.CartItemsDto;
import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/carts")
@RestController
public class CartController {
  private final CartService cartService;
  @PostMapping("/cartItems")
  public ResponseWrapper<Void> addCartItem(@LoginUser AuthenticatedUser authenticatedUser,
                                        @RequestBody AddCartItemRequest request) {
    cartService.addCartItem(authenticatedUser.getMemberId(), request);
    return ResponseWrapper.ok();
  }

  @GetMapping("/cartItems")
  public ResponseWrapper<List<CartItemsDto>> getCartItems(@LoginUser AuthenticatedUser authenticatedUser) {
    return ResponseWrapper.ok(cartService.getCartItems(authenticatedUser.getMemberId()));
  }

  @DeleteMapping("/cartItems")
  public ResponseWrapper<Void> addCartItem(@LoginUser AuthenticatedUser authenticatedUser,
      @RequestBody List<Long> cartItemsIds) {
    cartService.deleteCartItem(authenticatedUser.getMemberId(), cartItemsIds);
    return ResponseWrapper.ok();
  }

  @PutMapping("/cartItems/{id}/adjust-count")
  public ResponseWrapper<Void> adjustCartItemCount(@LoginUser AuthenticatedUser authenticatedUser,
      @PathVariable Long id, @RequestParam Integer count) {
    cartService.adjustCount(authenticatedUser.getMemberId(), id ,count);
    return ResponseWrapper.ok();
  }
}
