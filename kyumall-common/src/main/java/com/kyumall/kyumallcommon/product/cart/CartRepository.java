package com.kyumall.kyumallcommon.product.cart;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.cart.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartRepository extends JpaRepository<Cart, Long> {
  @Query("""
    select distinct c from Cart c
      left join fetch c.member m
      left join fetch c.cartItems ci
      left join fetch ci.product p
    where c.member = :member
    order by ci.createdAt
  """)
  Optional<Cart> findWithItemsByMember(Member member);
}
