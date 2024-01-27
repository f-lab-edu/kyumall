package com.kyumall.kyumallclient.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class SendVerificationEmailResponse {
  private String key;

  public static SendVerificationEmailResponse of(String key) {
    return new SendVerificationEmailResponse(key);
  }
}
