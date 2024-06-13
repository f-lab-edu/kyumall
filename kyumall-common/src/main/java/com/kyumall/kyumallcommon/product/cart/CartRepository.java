package com.kyumall.kyumallcommon.product.cart;

import com.kyumall.kyumallcommon.product.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

}
