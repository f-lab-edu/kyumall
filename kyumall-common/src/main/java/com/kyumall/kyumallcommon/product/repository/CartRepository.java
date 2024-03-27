package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

}
