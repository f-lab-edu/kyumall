package com.kyumall.kyumallclient.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class ResetPasswordRequest {
  private String email;
  private String username;
  private String password;
  private String newPassword;
  private String newPasswordConfirm;
}
