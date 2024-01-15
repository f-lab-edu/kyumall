package com.kyumall.kyumallclient.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifySentCodeRequest {
  private String email;
  private String code;
}
