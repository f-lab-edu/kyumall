package com.kyumall.kyumallcommon.auth;

import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticationService;
import com.kyumall.kyumallcommon.auth.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
  private final AuthenticationService authenticationService;
  private final JwtProvider jwtProvider;

  public String login(LoginRequest loginRequest) {
    AuthenticatedUser authenticatedUser = authenticationService.verifyUser(
        loginRequest.toSimpleUserInput());

    return jwtProvider.generateToken(authenticatedUser.getUsername());
  }
}
