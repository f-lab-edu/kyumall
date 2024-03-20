package com.kyumall.kyumallcommon.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyumall.kyumallcommon.auth.JwtProvider;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.auth.authentication.UserContext;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import io.jsonwebtoken.JwtParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticateFilter extends OncePerRequestFilter {
  private final JwtProvider jwtProvider;
  private final MemberRepository memberRepository;
  private final ObjectMapper objectMapper;
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    log.debug("-----AuthenticateFilter-----");

    try {
      try {
        String token = jwtProvider.resolveToken(request);
        processToken(token);
      } catch (KyumallException e) {
        handleExceptionInFilter(response, e);
      }
      doFilter(request, response, filterChain);
    } finally {
      UserContext.clear();
    }
  }

  private void handleExceptionInFilter(HttpServletResponse response, KyumallException e) {
    ErrorCode errorCode = e.getErrorCode();
    response.setStatus(errorCode.getHttpStatus());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    try(PrintWriter writer = response.getWriter()) {
      writer.write(objectMapper.writeValueAsString(new ResponseWrapper<>(errorCode.getCode(), errorCode.getMessage(), null) ));
    } catch (IOException ioException) {
      throw new RuntimeException(ioException);
    }
  }

  private void processToken(String token) {
    if (token != null) {
      JwtParser jwtParser = jwtProvider.getJwtParser(token);
      if (jwtProvider.validateClaim(jwtParser, token)) {
        String username = jwtProvider.getUsername(jwtParser, token);
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));

        UserContext.setUser(AuthenticatedUser.from(member));
      } else {
        throw new KyumallException(ErrorCode.INVALID_TOKEN);
      }
    }
  }
}
