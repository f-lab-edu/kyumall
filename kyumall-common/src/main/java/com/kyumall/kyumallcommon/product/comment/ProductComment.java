package com.kyumall.kyumallcommon.product.comment;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.entity.Member;
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

@Builder
@Getter @AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ProductComment extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;
  private String content;
  @ManyToOne
  @JoinColumn(name = "parent_id")
  private ProductComment parentComment;

  public void updateComment(String newComment) {
    this.content = newComment;
  }
}