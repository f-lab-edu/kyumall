package com.kyumall.kyumallcommon.member.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
  //private String groupName; // 약관 그룹명 (영문)
  //private Integer version;  // 버전
  private String name;  // 약관명
  @Lob
  @Column(columnDefinition = "TEXT")
  private String content; // 약관 내용
  //private Integer order;  // 약관 정렬 순서
  @Enumerated(EnumType.STRING)
  private TermType type;
  @Enumerated(EnumType.STRING)
  private TermStatus status;  // 현재 사용중인지 여부

  //TODO: 관리자 페이지에 약관 관리에 참고할 내용
  //TODO: 약관의 버전 (히스토리 관리)
  //TODO: 약관을 그룹핑 해서, 가장 최신 버전의 버전을 조회해야함
  //TODO: 약관 그룹
  //TODO: 약관 정렬 (프론트? 서버에서?, 서버에서 관리하는것이 좋음 ORDERING 정렬하기 위한 순서)

  /**
   * 해당 약관이 필수인지 반환합니다
   * @return
   */
  public boolean isRequired() {
    return type.equals(TermType.REQUIRED);
  }
}
