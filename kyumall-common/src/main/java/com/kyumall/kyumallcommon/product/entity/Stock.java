package com.kyumall.kyumallcommon.product.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
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

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Stock extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;
  private Long quantity;

  public void updateQuantity(long quantity) {
    validateQuantity(quantity);
    this.quantity = quantity;
  }

  void validateQuantity(long quantity) {
    if (quantity < 0) {
      throw new KyumallException(ErrorCode.STOCK_IS_INSUFFICIENT);
    }
  }

  public boolean decreasable(long toDecreaseQuantity) {
    return this.quantity >= toDecreaseQuantity;
  }

  public void decrease(long quantity) {
    validateQuantity(this.quantity - quantity);
    this.quantity -= quantity;
  }
}
