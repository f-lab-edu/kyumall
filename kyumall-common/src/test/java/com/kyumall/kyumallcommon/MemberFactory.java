package com.kyumall.kyumallcommon;

import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberFactory {
  @Autowired
  MemberRepository memberRepository;

  @Autowired
  PasswordService passwordService;

  public Member createMember(String username, String email, String password, MemberType memberType) {
    return memberRepository.saveAndFlush(Member.builder()
        .username(username)
        .email(email)
        .password(passwordService.encrypt(password))
        .status(MemberStatus.INUSE)
        .type(memberType)
        .build());
  }
}
