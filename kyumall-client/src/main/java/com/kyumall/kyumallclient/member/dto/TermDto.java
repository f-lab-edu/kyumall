package com.kyumall.kyumallclient.member.dto;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.vo.TermType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class TermDto {
  private Long termId;
  private String termName;
  private String content;
  private TermType type;

  public static TermDto from(Term term) {
    return TermDto.builder()
        .termId(term.getId())
        .termName(term.getName())
        .content(term.getContent())
        .type(term.getType())
        .build();
  }
}
