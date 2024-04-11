package com.kyumall.kyumallcommon.order.repository;

import com.kyumall.kyumallcommon.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
