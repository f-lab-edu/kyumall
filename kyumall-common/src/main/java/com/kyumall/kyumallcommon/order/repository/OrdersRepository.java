package com.kyumall.kyumallcommon.order.repository;

import com.kyumall.kyumallcommon.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

}
