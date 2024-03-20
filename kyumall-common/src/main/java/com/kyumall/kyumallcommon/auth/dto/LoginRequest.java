package com.kyumall.kyumallcommon.auth.dto;

import com.kyumall.kyumallcommon.auth.authentication.SimpleUserInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class LoginRequest {
  private String username;
  private String password;

  public SimpleUserInput toSimpleUserInput() {
    return SimpleUserInput.builder()
        .username(username)
        .password(password)
        .build();
  }
}
