package com.kyumall.kyumalladmin.member.dto;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class TermDto {
  private Long id;
  private String name;  // 약관명
  private Integer ordering;  // 약관 정렬 순서
  private TermType type;
  private TermStatus status;  // 현재 사용중인지 여부
  private LocalDateTime createdAt;

  public static TermDto from(Term term) {
    return TermDto.builder()
        .id(term.getId())
        .name(term.getName())
        .ordering(term.getOrdering())
        .type(term.getType())
        .status(term.getStatus())
        .createdAt(term.getCreatedAt())
        .build();
  }
}
