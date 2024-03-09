package com.kyumall.kyumallclient.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifySentCodeRequest {
  @Schema(description = "본인인증 시도한 이메일", example = "example@example.com")
  private String email;
  @Schema(description = "이메일로 발송된 본인인증코드", example = "102024")
  private String code;
  @Schema(description = "인증메일 발송시 반환된 key", example = "=Veka23f")
  private String verificationKey;
}
