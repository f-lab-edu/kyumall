package com.kyumall.kyumallcommon.product.product.entity;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.List;
import java.util.Objects;
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

  @Enumerated(EnumType.STRING)
  private ProductStatus productStatus;

  @OrderBy("sequence asc")
  @OneToMany(mappedBy = "product")
  private List<ProductImage> productImages;

  private String detail;

  public boolean isDeleted() {
    return productStatus == ProductStatus.DELETED;
  }

  public Product delete() {
    this.productStatus = ProductStatus.DELETED;
    return this;
  }

  public boolean isCategoryChanged(Long newCategoryId) {
    return !Objects.equals(this.category.getId(), newCategoryId);
  }

  public void changeCategory(Category category) {
    this.category = category;
  }

  public boolean isSeller(Long memberId) {
    return Objects.equals(this.seller.getId(), memberId);
  }

  public void changeInfo(String productName, Integer price, String detail) {
    this.name = productName;
    this.price = price;
    this.detail = detail;
  }

  /**
   * 대표 이미지 조회
   * 이미지 sequence 가 가장 빠른 이미지를 대표이미지로 삼는다.
   * 이미지가 없는 경우, null을 반환한다.
   * @return
   */
  public String getRepresentativeImage() {
    if (this.productImages.isEmpty()) {
      return null;
    }
    return this.productImages.get(0).getImageId();
  }
}
