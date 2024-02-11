package com.kyumall.kyumallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class CreatedIdDto {
  private Long id;
  public static CreatedIdDto of(Long id) {
    return new CreatedIdDto(id);
  }
}
