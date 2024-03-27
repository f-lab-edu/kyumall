package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
