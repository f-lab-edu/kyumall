package com.kyumall.kyumallclient.member.dto;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import com.kyumall.kyumallcommon.member.vo.TermType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class TermDto {
  private Long termId;
  private String title;
  private String content;
  private TermType type;

  public static TermDto of(Term term, TermDetail latestTermDetail) {
    return TermDto.builder()
        .termId(term.getId())
        .title(latestTermDetail.getTitle())
        .content(latestTermDetail.getContent())
        .type(term.getType())
        .build();
  }
}
