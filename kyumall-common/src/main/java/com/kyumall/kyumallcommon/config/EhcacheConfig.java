package com.kyumall.kyumallcommon.config;

import java.time.Duration;
import java.util.Map;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EhcacheConfig {
  @Bean
  public CacheManager ehcacheManager() {
    CacheConfiguration<String, Map> cacheConfig = CacheConfigurationBuilder
        .newCacheConfigurationBuilder(String.class, Map.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                .offheap(50, MemoryUnit.MB)
                .build())
        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofDays(1)))
        .build();

    CachingProvider cachingProvider = Caching.getCachingProvider();
    javax.cache.CacheManager cacheManager = cachingProvider.getCacheManager();

    javax.cache.configuration.Configuration<String, Map> configuration = Eh107Configuration.fromEhcacheCacheConfiguration(
        cacheConfig);
    cacheManager.createCache("categoryGroupByParentMap", configuration);
    return cacheManager;
  }

}
