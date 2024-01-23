package com.kyumall.kyumallclient.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class RecoverPasswordRequest {
  @NotEmpty
  private String username;
  @NotEmpty @Email
  private String email;
}
