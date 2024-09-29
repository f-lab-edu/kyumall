package com.kyumall.kyumallcommon.auth.authentication;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecurityIgnorePathsTest {

  @Test
  @DisplayName("무시해야할 경로를 추가합니다.")
  void SecurityIgnorePathsBuilder_add_success() {
    String path1 = "/path1";
    String path2 = "/path2";

    SecurityIgnorePaths securityIgnorePaths = SecurityIgnorePaths.ignore()
        .add(path1)
        .add(path2)
        .build();

    assertThat(securityIgnorePaths.getIgnorePaths())
        .contains(path1)
        .contains(path2);
  }

  @Test
  @DisplayName("패턴 방식으로 무시해야할 경로인지 판별에 성공합니다.")
  public void SecurityIgnorePathsBuilder_shouldIgnore_pattern_match_success() {
    String postFixPattern = "/path1/**";
    String bothPattern = "/**/login/**";      // 맨앞에 / 가 있어야한다.
    SecurityIgnorePaths securityIgnorePaths = SecurityIgnorePaths.ignore()
        .add(postFixPattern)
        .add(bothPattern)
        .build();
//    String paramUri = "/path1?userId=1";    // httpServletRequest 에서 getUri() 를 호출하면 queryParam 이 제거되어 들어온다.
    String subUri = "/path1/v1/test";
    String loginUri = "/api/auth/login";

//    assertThat(securityIgnorePaths.shouldIgnore(paramUri)).isTrue();
    assertThat(securityIgnorePaths.shouldIgnore(subUri)).isTrue();
    assertThat(securityIgnorePaths.shouldIgnore(loginUri)).isTrue();
  }
}
