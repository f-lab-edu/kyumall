package com.kyumall.kyumalladmin.member.dto;

import com.kyumall.kyumallcommon.member.entity.TermDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class TermDetailDto {
  private Long id;
  private String title;
  private String content;
  private Integer version;
  public static TermDetailDto from(TermDetail detail) {
    return TermDetailDto.builder()
        .id(detail.getId())
        .title(detail.getTitle())
        .content(detail.getContent())
        .version(detail.getVersion())
        .build();
  }
}
