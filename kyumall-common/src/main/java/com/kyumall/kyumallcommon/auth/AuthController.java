package com.kyumall.kyumallcommon.auth;

import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.auth.authentication.UserContext;
import com.kyumall.kyumallcommon.auth.dto.LoginRequest;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<ResponseWrapper<Void>> login(@RequestBody LoginRequest loginRequest) {
    String token = authService.login(loginRequest);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    return ResponseWrapper.ok(headers);
  }

  @GetMapping("/auth-by-token")
  public ResponseWrapper<AuthenticatedUser> authByToken(@LoginUser AuthenticatedUser authenticatedUser) {
    return ResponseWrapper.ok(authenticatedUser);
  }
}
