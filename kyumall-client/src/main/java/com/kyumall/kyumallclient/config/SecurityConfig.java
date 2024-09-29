package com.kyumall.kyumallclient.config;

import com.kyumall.kyumallcommon.auth.authentication.SecurityIgnorePaths;
import com.kyumall.kyumallcommon.auth.authentication.SecurityIgnorePaths.IgnoreStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityIgnorePaths securityIgnorePaths() {
    return SecurityIgnorePaths.ignore()
        .add("/**", IgnoreStrategy.PERMIT_ANONYMOUS)  // 익명유저허용
        .build();
  }
}
