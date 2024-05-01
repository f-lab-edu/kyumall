package com.kyumall.kyumallcommon.order.repository;

import com.kyumall.kyumallcommon.order.entity.OrderGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<OrderGroup, Long> {
  @Query("select distinct og "
      + " from OrderGroup og "
      + " join fetch og.orders o "
      + " join fetch og.buyer "
      + " where og.id = :id")
  Optional<OrderGroup> findWithOrdersById(Long id);
}
