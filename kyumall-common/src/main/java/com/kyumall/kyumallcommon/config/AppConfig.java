package com.kyumall.kyumallcommon.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
  /**
   * LocalDateTime.now() 를 모킹하기 위해 Clock 객체를 빈으로 등록합니다.
   * 현재시간을 테스트에서 모킹하기 위함입니다.
   * @return
   */
  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
