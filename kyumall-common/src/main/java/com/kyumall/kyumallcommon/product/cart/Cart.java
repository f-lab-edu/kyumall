package com.kyumall.kyumallcommon.product.cart;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.product.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Entity
public class Cart {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne
  @JoinColumn(name = "member_id")
  private Member member;
  @Builder.Default
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> cartItems = new ArrayList<>();

  public void addCart(CartItem cartItem) {
    cartItems.add(cartItem);
  }

  public void addCart(Product product, int count) {
    cartItems.add(CartItem.builder()
            .cart(this)
            .product(product)
            .count(count)
        .build());
  }

  public boolean existsInCart(Product product) {
    return cartItems.stream().anyMatch(cartItem -> Objects.equals(cartItem.getProduct().getId(),
        product.getId()));
  }

  public Optional<CartItem> getCartItemByProduct(Product product) {
    return cartItems.stream().filter(cartItem -> cartItem.getId().equals(product.getId())).findAny();
  }

  public void deleteCartItems(List<Long> cartItemIds) {
    cartItemIds.forEach(this::deleteCartItem);
  }

  public void deleteCartItem(Long cartItemId) {
    cartItems.removeIf(cartItem -> cartItem.getId().equals(cartItemId));
  }

  public Optional<CartItem> getCartItem(Long cartItemId) {
    return cartItems.stream().filter(cartItem -> cartItem.getId().equals(cartItemId)).findAny();
  }
}
