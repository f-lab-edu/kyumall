package com.kyumall.kyumallcommon.config;

import feign.Logger;
import feign.Logger.Level;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@EnableFeignClients("com.kyumall")
@Configuration
public class FeignConfig {
  @Bean
  @Profile("local")
  Logger.Level feignLoggerLevel() {
    return Level.FULL;
  }
}
