package com.kyumall.kyumallcommon.product.cart.dto;

import com.kyumall.kyumallcommon.product.cart.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class CartItemsDto {
  private Long cartItemId;
  private Long productId;
  private String productName;
  private Integer price;
  private String image;
  private Integer count;

  public static CartItemsDto from(CartItem cartItem) {
    return CartItemsDto.builder()
        .cartItemId(cartItem.getId())
        .productId(cartItem.getProduct().getId())
        .productName(cartItem.getProduct().getName())
        .price(cartItem.getProduct().getPrice())
        .image(cartItem.getProduct().getImage())
        .count(cartItem.getCount())
        .build();
  }
}
