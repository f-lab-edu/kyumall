package com.kyumall.kyumallcommon.order.repository;

import com.kyumall.kyumallcommon.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
