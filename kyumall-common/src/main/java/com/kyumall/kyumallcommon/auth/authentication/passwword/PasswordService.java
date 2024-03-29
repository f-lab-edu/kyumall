package com.kyumall.kyumallcommon.auth.authentication.passwword;

public interface PasswordService {
  String encrypt(String originalPassword);
  boolean isMath(String plainPassword, String hashedPassword);
}
