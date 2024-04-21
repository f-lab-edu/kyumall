package com.kyumall.kyumallcommon.product.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import jakarta.persistence.Entity;
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

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Stock extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;
  private Long quantity;

  public void updateQuantity(long quantity) {
    validateQuantity(quantity);
    this.quantity = quantity;
  }

  void validateQuantity(long quantity) {
    if (quantity < 0) {
      throw new IllegalArgumentException("재고는 0 미만 일 수 없습니다.");
    }
  }
}
