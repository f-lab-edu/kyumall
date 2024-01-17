package com.kyumall.kyumallcommon.member.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Member extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private String email;
  private String password;
  @Enumerated(value = EnumType.STRING)
  private MemberType type;
  @Enumerated(value = EnumType.STRING)
  private MemberStatus status;
}
