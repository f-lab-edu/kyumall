package com.kyumall.kyumallcommon.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CaffeineCacheConfig {
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();

    // default 설정 등록
    Caffeine<Object, Object> caffeine = Caffeine.newBuilder().recordStats()
        .expireAfterWrite(10, TimeUnit.MINUTES);
    cacheManager.setCaffeine(caffeine);

    // custom 설정 등록
    Arrays.stream(CaffeineCacheEnum.values()).forEach(
        cacheEnum -> cacheManager.registerCustomCache(cacheEnum.getName(), cacheEnum.getCache()));

    return cacheManager;
  }
}
