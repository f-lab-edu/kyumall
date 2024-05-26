package com.kyumall.kyumallcommon.factory;

import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
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

  public Member createMember(MemberFixture memberFixture) {
    return memberRepository.saveAndFlush(memberFixture.createEntity(passwordService));
  }
}
