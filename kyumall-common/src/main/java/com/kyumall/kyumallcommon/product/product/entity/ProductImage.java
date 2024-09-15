package com.kyumall.kyumallcommon.product.product.entity;

import com.kyumall.kyumallcommon.upload.entity.Image;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@IdClass(ProductImageKey.class)
@AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED) @Builder
@Getter @Entity
public class ProductImage {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "image_id")
  private Image image;

  private Integer sequence;  // 이미지 정렬 순서, 순서가 가장 빠른 이미지가 대표 이미지 입니다.
}
