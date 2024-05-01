package com.kyumall.kyumallcommon.order.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.entity.Product;
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
public class OrderGroup extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id")
  private Member buyer;
  private LocalDateTime orderDatetime;
  @Builder.Default
  @OneToMany(mappedBy = "orderGroup", fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Orders> orders = new ArrayList<>();

  public void addOrder(Product product, int count) {
    orders.add(Orders.from(product, this, count));
  }

  public void addOrders(List<Product> products, List<Integer> counts) {
    IntStream.range(0, products.size())
        .forEach(index -> {
          Product product = products.get(index);
          Integer count = counts.get(index);
          addOrder(product, count);
        });
  }

  public long calculateTotalPrice() {
    long totalPrice = 0;
    for (Orders orders : this.orders) {
      totalPrice += (orders.getOrderPrice() * orders.getCount());
    }
    return totalPrice;
  }

  public void payComplete() {
    for (Orders order: orders) {
      order.payComplete();
    }
  }
}
