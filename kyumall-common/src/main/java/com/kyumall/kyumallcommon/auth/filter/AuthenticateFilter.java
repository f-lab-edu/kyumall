package com.kyumall.kyumallcommon.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyumall.kyumallcommon.auth.JwtProvider;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.auth.authentication.SecurityIgnorePaths;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 인증여부를 체크하는 필터입니다.
 * Request 에서 인증 토큰의 유무를 체크합니다.
 * securityIgnorePaths 에 명시된 경로는 토큰 체크를 하지 않습니다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticateFilter extends OncePerRequestFilter {
  private final JwtProvider jwtProvider;
  private final MemberRepository memberRepository;
  private final ObjectMapper objectMapper;
  private final SecurityIgnorePaths securityIgnorePaths;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    log.debug("-----AuthenticateFilter-----");

    try {
      try {
        if (!shouldSkipAuthenticate(request)) {    // 인증을 건너뛰어야하는지 확인
          String token = jwtProvider.resolveToken(request);
          boolean isValid = processToken(token);
          if (!isValid && !shouldPermitAnonymous(request)) {  // 익명유저 permit 체크
            throw new KyumallException(ErrorCode.INVALID_TOKEN);
          }
        }
        doFilter(request, response, filterChain); // doFilter 는 이전 프로세스가 성공한 경우에만 호출되도록 해야함
      } catch (KyumallException e) {
        handleExceptionInFilter(response, e);
      }
    } finally {
      UserContext.clear();
    }
  }

  private boolean shouldPermitAnonymous(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return securityIgnorePaths.shouldPermitAnonymous(uri);
  }

  private boolean shouldSkipAuthenticate(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return securityIgnorePaths.shouldSkipAuthenticate(uri);
  }

  /**
   * 필터에서 발생한 에러를 표준응답에 맞추어 반환합니다.
   * 필터에서는 GlobalExceptionHandler 로 요청이 전달되지 않기에 추가하였습니다.
   * @param response
   * @param e
   */
  private void handleExceptionInFilter(HttpServletResponse response, KyumallException e) {
    log.error("handleExceptionInFilter ", e);
    ErrorCode errorCode = e.getErrorCode();
    response.setStatus(errorCode.getHttpStatus());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    ResponseWrapper<Object> errorResponse = new ResponseWrapper<>(errorCode.getCode(),
        errorCode.getMessage(), null);

    try {
      String jsonResponse = objectMapper.writeValueAsString(errorResponse);
      response.getWriter().write(jsonResponse);
      response.getWriter().flush();
    } catch (IOException ex) {
      log.error("응답 반환중 에러 발생", ex);
    }
  }

  private boolean processToken(String token) {
    if (token == null) {
      return false;
    }
    JwtParser jwtParser = jwtProvider.getJwtParser(token);
    if (!jwtProvider.validateClaim(jwtParser, token)) {
      return false;
    }
      String username = jwtProvider.getUsername(jwtParser, token);
    //TODO: 매번 사용자를 체크하는데 맞을까? JWT 토큰의 장점이 없다
      Member member = memberRepository.findByUsername(username)
          .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));

      UserContext.setUser(AuthenticatedUser.from(member));
      return true;
  }
}
