package com.kyumall.kyumalladmin.config;

import com.kyumall.kyumallcommon.auth.authentication.SecurityIgnorePaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityIgnorePaths securityIgnorePaths() {
    return SecurityIgnorePaths.ignore()
        .add("/**/login/**")
        .build();
  }
}
