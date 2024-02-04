package com.kyumall.kyumallcommon.member.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
public class Term extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;  // 약관명
  private Integer ordering;  // 약관 정렬 순서
  @Enumerated(EnumType.STRING)
  private TermType type;
  @Enumerated(EnumType.STRING)
  private TermStatus status;  // 현재 사용중인지 여부
  @OneToMany(mappedBy = "term")
  private List<TermDetail> termDetails;

  /**
   * 해당 약관이 필수인지 반환합니다
   * @return
   */
  public boolean isRequired() {
    return type.equals(TermType.REQUIRED);
  }

  public void update(String name, Integer ordering, TermType type, TermStatus status) {
    this.name = name;
    this.ordering = ordering;
    this.type = type;
    this.status = status;
  }
}
