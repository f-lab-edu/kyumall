package com.kyumall.kyumallcommon.product.product;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Product extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private Member seller;
  private String name;
  private Integer price;
//  @JoinColumn
//  @ManyToOne(fetch = FetchType.LAZY)
//  private Image image;
  @Enumerated(EnumType.STRING)
  private ProductStatus productStatus;
  private String image;
  private String detail;

  public boolean isDeleted() {
    return productStatus == ProductStatus.DELETED;
  }

  public Product delete() {
    this.productStatus = ProductStatus.DELETED;
    return this;
  }
}
