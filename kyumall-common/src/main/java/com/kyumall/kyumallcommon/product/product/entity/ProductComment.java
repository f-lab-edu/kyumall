package com.kyumall.kyumallcommon.product.product.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.entity.Member;
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
  public static final String COMMENT_DELETED_BY_ADMIN = "관리자에 의해 삭제된 댓글입니다.";
  public static final String COMMENT_DELETED = "삭제된 댓글입니다.";

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
  @Builder.Default
  private boolean deleted = false;    // 삭제 여부
  @Builder.Default
  private boolean deletedByAdmin = false; // 관리자에 의해 삭제 여부

  public void updateComment(String newComment) {
    this.content = newComment;
  }

  public void delete() {
    this.deleted = true;
  }

  public void deleteByAdmin() {
    delete();
    this.deletedByAdmin = true;
  }

  /**
   * 삭제 코멘트를 적용한 댓글 내용
   * 삭제된 댓글의 경우 삭제 코멘트로 대체하여 반환
   * @return
   */
  public String getContentApplyDeleted() {
    if (deletedByAdmin) {
      return COMMENT_DELETED_BY_ADMIN;
    }
    if (deleted) {
      return COMMENT_DELETED;
    }
    return this.content;
  }
}
