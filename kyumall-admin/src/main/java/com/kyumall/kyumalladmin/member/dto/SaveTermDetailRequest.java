package com.kyumall.kyumalladmin.member.dto;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class SaveTermDetailRequest {
  @NotNull
  private Long termId;
  @NotEmpty
  private String title;
  @NotEmpty
  private String content;
  @NotNull @Min(1)
  private Integer version;

  public TermDetail toEntity(Term term) {
    return TermDetail.builder()
        .term(term)
        .title(title)
        .content(content)
        .version(version)
        .build();
  }
}
