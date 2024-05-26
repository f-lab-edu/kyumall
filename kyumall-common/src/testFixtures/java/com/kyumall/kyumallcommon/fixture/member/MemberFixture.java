package com.kyumall.kyumallcommon.fixture.member;

import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberFixture {
  // client
  KIM("userKim01", "kim01@example.com", MemberStatus.INUSE, MemberType.CLIENT),
  PARK("userPart01", "park01@example.com", MemberStatus.INUSE, MemberType.CLIENT),
  // seller
  LEE("userLee01", "lee01@example.com", MemberStatus.INUSE, MemberType.SELLER),
  ;

  private final String username;
  private final String email;
  private final MemberStatus status;
  private final MemberType type;

  public static final String password = "random_password123!";

  public Member createEntity(PasswordService passwordService) {
    return Member.builder()
        .username(username)
        .email(email)
        .password(passwordService.encrypt(password))
        .status(status)
        .type(type)
        .build();
  }
}
