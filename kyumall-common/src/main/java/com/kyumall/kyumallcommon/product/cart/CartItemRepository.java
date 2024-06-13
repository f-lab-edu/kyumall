package com.kyumall.kyumallcommon.product.cart;

import com.kyumall.kyumallcommon.product.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
