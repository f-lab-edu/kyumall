package com.kyumall.kyumallcommon.order.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor @Builder @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Orders extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id")
  private Member buyer;

  private LocalDateTime orderDatetime;
  @Builder.Default
  @OneToMany(mappedBy = "orders", fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  public void addProduct(Product product, int count) {
    orderItems.add(OrderItem.from(product, this, count));
  }

  public void addProducts(List<Product> products, List<Integer> counts) {
    IntStream.range(0, products.size())
        .forEach(index -> {
          Product product = products.get(index);
          Integer count = counts.get(index);
          addProduct(product, count);
        });
  }

  public long calculateTotalPrice() {
    long totalPrice = 0;
    for (OrderItem orderItem : this.orderItems) {
      totalPrice += (orderItem.getOrderPrice() * orderItem.getCount());
    }
    return totalPrice;
  }
}
