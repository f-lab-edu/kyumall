package com.kyumall.kyumallclient.member;

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

  public Member createMember(String username, String email, String password, MemberType memberType) {
    return memberRepository.saveAndFlush(Member.builder()
            .username(username)
            .email(email)
            .password(password)
            .status(MemberStatus.INUSE)
            .type(memberType)
        .build());
  }
}
