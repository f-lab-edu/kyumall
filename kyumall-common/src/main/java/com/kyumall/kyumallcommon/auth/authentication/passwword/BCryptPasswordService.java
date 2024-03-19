package com.kyumall.kyumallcommon.auth.authentication.passwword;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class BCryptPasswordService implements PasswordService {

  @Override
  public String encrypt(String originalPassword) {
    return BCrypt.hashpw(originalPassword, BCrypt.gensalt());
  }

  @Override
  public boolean isMath(String plainPassword, String hashedPassword) {
    return BCrypt.checkpw(plainPassword, hashedPassword);
  }
}
