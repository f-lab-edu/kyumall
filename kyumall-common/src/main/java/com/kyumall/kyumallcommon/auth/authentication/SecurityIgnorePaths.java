package com.kyumall.kyumallcommon.auth.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * 토큰체크 무시 경로
 * 토큰을 체크하는 필터를 통과하지 않는 Path 를 명시
 */
public class SecurityIgnorePaths {
  private final List<PathAndStrategy> pathAndStrategies;
  private final PathMatcher pathMatcher;

  public SecurityIgnorePaths() {
    this.pathAndStrategies = new ArrayList<>();
    this.pathMatcher = new AntPathMatcher();
  }

  /**
   * 무시할 경로를 추가합니다.
   * @param path
   */
  public void add(String path, IgnoreStrategy ignoreStrategy) {
    pathAndStrategies.add(new PathAndStrategy(path, ignoreStrategy));
  }

  public void add(String path) {
    pathAndStrategies.add(new PathAndStrategy(path, IgnoreStrategy.SKIP_AUTHENTICATION));
  }

  /**
   * 인증 과정을 건너뛰어야하는지 판별합니다.
   * uri 패턴 체크, SKIP_AUTHENTICATION 체크
   * @return
   */
  public boolean shouldSkipAuthenticate(String uri) {
    return pathAndStrategies.stream().anyMatch(pathAndStrategy ->
        pathMatcher.match(pathAndStrategy.path, uri)
        && pathAndStrategy.ignoreStrategy == IgnoreStrategy.SKIP_AUTHENTICATION);
  }

  /**
   * 익명 유저를 허락해야하는지 반환합니다.
   * @param uri
   * @return
   */
  public boolean shouldPermitAnonymous(String uri) {
    return pathAndStrategies.stream().anyMatch(pathAndStrategy ->
        pathMatcher.match(pathAndStrategy.path, uri)
            && pathAndStrategy.ignoreStrategy == IgnoreStrategy.PERMIT_ANONYMOUS);
  }

  public List<String> getIgnorePaths() {
    return this.pathAndStrategies.stream().map(PathAndStrategy::getPath).collect(Collectors.toList());
  }

  /**
   * 경로와 IgnoreStrategy 를 같이 가지는 클래스
   */
  public static class PathAndStrategy {
    private final String path;
    private IgnoreStrategy ignoreStrategy;


    public PathAndStrategy(String path, IgnoreStrategy ignoreStrategy) {
      this.path = path;
      this.ignoreStrategy = ignoreStrategy;
    }

    public PathAndStrategy(String path) {
      this.path = path;
      this.ignoreStrategy = IgnoreStrategy.SKIP_AUTHENTICATION;
    }

    public void changeStrategy(IgnoreStrategy ignoreStrategy) {
      this.ignoreStrategy = ignoreStrategy;
    }

    public String getPath() {
      return path;
    }

    public IgnoreStrategy getIgnoreStrategy() {
      return ignoreStrategy;
    }
  }

  public enum IgnoreStrategy {
    SKIP_AUTHENTICATION, // 인증 과정 스킵
    PERMIT_ANONYMOUS    // 익명의 사용자를 허용합니다.
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

    public SecurityIgnorePathsBuilder add(String path, IgnoreStrategy ignoreStrategy) {
      this.securityIgnorePaths.add(path, ignoreStrategy);
      return this;
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
