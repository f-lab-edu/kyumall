package com.kyumall.kyumallcommon.auth.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 인증을 위한 입력값 객체
 */
@AllArgsConstructor @Builder
@Getter
public class SimpleUserInput {
  private String username;
  private String password;
}
