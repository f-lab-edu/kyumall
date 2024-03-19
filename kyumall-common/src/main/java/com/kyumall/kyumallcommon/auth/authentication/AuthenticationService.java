package com.kyumall.kyumallcommon.auth.authentication;

/**
 * 인증 역할을 부여 받은 서비스
 */
public interface AuthenticationService {
  AuthenticatedUser verifyUser(Object userInput);
}
