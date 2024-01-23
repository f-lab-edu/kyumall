package com.kyumall.kyumallcommon.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class Mail {
  private String to;
  private String subject;
  private String message;
}
