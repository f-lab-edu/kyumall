package com.kyumall.kyumallclient.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter @AllArgsConstructor
public class SendVerificationEmailResponse {
  @Schema(example = "QVYcHEH6QDoXIWTxreA5jA==", description = "메일 전송 객체의 ID를 암호화 한 값")
  private String key;

  public static SendVerificationEmailResponse of(String key) {
    return new SendVerificationEmailResponse(key);
  }
}
