package com.kyumall.kyumallcommon.product.category;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.product.product.Product;
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
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Getter
@NoArgsConstructor
@AllArgsConstructor @Builder
@Entity
public class Category extends BaseTimeEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @Enumerated(value = EnumType.STRING)
  private CategoryStatus status;

  @OneToMany(mappedBy = "category")
  private List<Product> products;

  public Long getParentId() {
    if (parent == null) {
      return getParentIdAsZeroIfRoot();
    }
    return parent.getId();
  }

  /**
   * newParentId 와 기존의 ParentId가 다른지 판별합니다.
   * @param newParentId
   * @return
   */
  public boolean isParentChanged(Long newParentId) {
    if (newParentId == null) {
      return false;
    }
    return !Objects.equals(getParentId(), newParentId);
  }

  public void changeParent(Category newParent) {
    this.parent = newParent;
  }

  public void changeName(String newName) {
    if (!name.equals(newName)) {
      this.name = newName;
    }
  }

  /**
   * 루트 카테고리의 경우, ParentID 를 0으로 반환합니다.
   */
  public Long getParentIdAsZeroIfRoot() {
    return 0L;
  }
}
