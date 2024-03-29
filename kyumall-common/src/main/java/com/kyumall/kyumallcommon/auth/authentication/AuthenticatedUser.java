package com.kyumall.kyumallcommon.auth.authentication;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 인증 완료된 회원 정보 객체
 */
@Getter
@AllArgsConstructor @Builder
public class AuthenticatedUser {
  private String username;
  private String email;
  private MemberType memberType;

  public static AuthenticatedUser from(Member member) {
    return AuthenticatedUser.builder()
        .username(member.getUsername())
        .email(member.getEmail())
        .memberType(member.getType())
        .build();
  }
}
