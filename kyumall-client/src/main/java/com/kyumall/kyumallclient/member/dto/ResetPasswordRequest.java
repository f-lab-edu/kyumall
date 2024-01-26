package com.kyumall.kyumallclient.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class ResetPasswordRequest {
  @NotEmpty @Email
  private String email;
  @NotEmpty
  private String username;
  @NotEmpty
  private String password;
  @NotEmpty
  private String newPassword;
  @NotEmpty
  private String newPasswordConfirm;
}
