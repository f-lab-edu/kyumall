package com.kyumall.kyumalladmin.member.dto;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class SaveTermRequest {
  @NotEmpty
  private String name;
  @NotNull
  private TermType type;
  @NotNull
  private TermStatus status;
  @NotNull @Min(1)
  private Integer ordering;

  public Term toEntity() {
    return Term.builder()
        .name(name)
        .type(type)
        .status(status)
        .ordering(ordering).build();
  }
}
