package com.kyumall.kyumallcommon.fixture.member;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TermDetailFixture {
  PRIVACY_DETAIL("개인정보 이용 약관 (필수)", "[ 개인정보 이용 약관 ] 제1장 총칙 제 1 조", 1),
  MARKETING_DETAIL_1("마케팅 목적의 개인정보 수집 및 이용 동의 (선택)", "마케팅 목적으로 개인정보를 수집하고 이용하는 것에 동의합니다.", 1),
  MARKETING_DETAIL_2("마케팅 목적 이용 동의 약관(선택)", "마케팅 목적으로 개인정보를 수집하고 이용하는 것에 동의합니다.", 2),
  ;

  private final String title;
  private final String content;
  private final int version;

  public TermDetail createEntity(Term term) {
    return TermDetail.builder()
        .term(term)
        .title(title)
        .content(content)
        .version(version)
        .build();
  }
}
