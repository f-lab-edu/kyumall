package com.kyumall.kyumallcommon.order.repository;

import com.kyumall.kyumallcommon.order.entity.Orders;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
  @Query("select distinct o "
      + " from Orders o "
      + " join fetch o.orderItems oi "
      + " join fetch o.buyer "
      + " where o.id = :id")
  Optional<Orders> findWithOrderItemsById(Long id);
}
