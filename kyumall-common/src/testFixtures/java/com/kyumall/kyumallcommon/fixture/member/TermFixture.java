package com.kyumall.kyumallcommon.fixture.member;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TermFixture {
  PRIVACY("개인정보 이용약관", 1, TermType.REQUIRED, TermStatus.INUSE),
  MARKETING("마케팅 동의 약관", 2, TermType.OPTIONAL, TermStatus.INUSE),
  ;

  private final String name;
  private final int ordering;
  private final TermType termType;
  private final TermStatus termStatus;

  public Term createEntity() {
    return Term.builder()
        .name(name)
        .ordering(ordering)
        .type(termType)
        .status(termStatus).build();
  }
}
