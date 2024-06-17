package com.kyumall.kyumallcommon.config;

import com.kyumall.kyumallcommon.auth.authentication.passwword.BCryptPasswordService;
import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "com.kyumall.kyumallcommon.factory")    // 테스트 유틸
public class TestConfig {
  @Bean
  public PasswordService passwordService() {
    return new BCryptPasswordService();
  }
}
