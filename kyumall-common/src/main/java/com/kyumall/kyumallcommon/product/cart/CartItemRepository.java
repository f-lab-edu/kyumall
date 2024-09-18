package com.kyumall.kyumallcommon.product.cart;

import com.kyumall.kyumallcommon.product.cart.CartItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  @Query("""
  select distinct ci 
  from CartItem ci
    join fetch ci.product
    left join fetch ci.product.productImages
  where ci.cart = :cart
""")
  List<CartItem> findWithProductImageByCart(Cart cart);
}
