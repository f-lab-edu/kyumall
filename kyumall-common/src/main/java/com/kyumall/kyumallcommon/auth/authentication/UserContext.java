package com.kyumall.kyumallcommon.auth.authentication;

/**
 * 스레드 별로 인증된 정보를 저장하고 있는 클래스
 */
public class UserContext {
  private static ThreadLocal<AuthenticatedUser> authThreadLocal = new ThreadLocal<>();

  public static void setUser(AuthenticatedUser authenticatedUser) {
    authThreadLocal.set(authenticatedUser);
  }

  public static AuthenticatedUser getUser() {
    return authThreadLocal.get();
  }

  public static void clear() {
    authThreadLocal.remove();
  }
}
