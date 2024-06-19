package com.kyumall.kyumallcommon.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.kyumall.kyumallcommon.product.category.Category;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Getter
public enum CaffeineCacheEnum {
  CATEGORY_GROUP_BY_PARENT_MAP("categoryGroupByParentMap",
      Caffeine.newBuilder()
          .recordStats()
          .expireAfterWrite(1, TimeUnit.DAYS)
          .evictionListener((Object key, Object value,
              RemovalCause cause) ->
              log.info(String.format(
                  "Key %s was evicted (%s)%n", key, cause)))
          .weigher((String key, Map<Long, List<Category>> value) -> value.size())
          .maximumWeight(10000)
          .build());

  private final String name;
  private final Cache cache;
}
