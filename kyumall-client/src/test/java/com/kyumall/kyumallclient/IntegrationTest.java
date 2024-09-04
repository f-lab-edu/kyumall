package com.kyumall.kyumallclient;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@Sql("/truncate.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {
  @LocalServerPort
  int port;

  @Autowired
  private CacheManager cacheManager;

  @BeforeEach
  public void setUp() {
    RestAssured.port = port;
  }

  /**
   * (매번 테스트 끝난 후 수행) 캐시된 객체가 있을 경우 캐시를 비운다.
   * BeforeEach 에서 수행할 경우, 캐시가 미리 정의되어 있지 않을 때
   * 이전에 생성된 캐시가 남아서 테스트에 영향을 미치기 때문에 AfterEach 에서 비워준다.
   */
  @AfterEach
  public void clearCache() {
    // 캐시 비우기
    cacheManager.getCacheNames().forEach(cacheName -> {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
      }
    });
  }
}
