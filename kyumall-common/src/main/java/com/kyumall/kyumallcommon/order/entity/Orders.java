package com.kyumall.kyumallcommon.order.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.product.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Orders extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderGroup orderGroup;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;
  private Integer count;
  private Integer orderPrice;

  public static Orders from(Product product, OrderGroup orderGroup, int count) {
    return Orders.builder()
        .orderGroup(orderGroup)
        .product(product)
        .count(count)
        .orderPrice(product.getPrice())
        .build();
  }
}
