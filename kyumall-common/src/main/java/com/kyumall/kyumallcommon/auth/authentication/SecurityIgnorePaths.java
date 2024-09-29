package com.kyumall.kyumallcommon.auth.authentication;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * 토큰체크 무시 경로
 * 토큰을 체크하는 필터를 통과하지 않는 Path 를 명시
 */
public class SecurityIgnorePaths {
  private final List<String> ignorePaths;
  private final PathMatcher pathMatcher;

  public SecurityIgnorePaths() {
    this.ignorePaths = new ArrayList<>();
    this.pathMatcher = new AntPathMatcher();
  }

  /**
   * 무시할 경로를 추가합니다.
   * @param path
   */
  public void add(String path) {
    ignorePaths.add(path);
  }

  /**
   * 무시해야하는 경로인지 판별합니다.
   * @return
   */
  public boolean shouldIgnore(String uri) {
    return ignorePaths.stream().anyMatch(ignorePath -> pathMatcher.match(ignorePath, uri));
  }

  public List<String> getIgnorePaths() {
    return this.ignorePaths;
  }

  /**
   * 빌더 클래스
   * @return
   */
  public static SecurityIgnorePathsBuilder ignore() {
    return new SecurityIgnorePathsBuilder();
  }

  public static class SecurityIgnorePathsBuilder {
    private final SecurityIgnorePaths securityIgnorePaths;

    public SecurityIgnorePathsBuilder() {
      this.securityIgnorePaths =  new SecurityIgnorePaths();
    }

    public SecurityIgnorePathsBuilder add(String path) {
      this.securityIgnorePaths.add(path);
      return this;
    }

    public SecurityIgnorePaths build() {
      return this.securityIgnorePaths;
    }
  }
}
