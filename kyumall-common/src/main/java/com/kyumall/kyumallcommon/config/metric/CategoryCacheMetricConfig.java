package com.kyumall.kyumallcommon.config.metric;

import com.kyumall.kyumallcommon.product.entity.Category;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
public class CategoryCacheMetricConfig {
  @Bean
  public MeterBinder categoryMapCacheCount(CacheManager cacheManager) {
    return registry -> Gauge.builder("kyumall.cache.categoryGroupByParentMap.count", cacheManager, manager -> {
      Map<Long, List<Category>> categoryMap = extractValueFromCacheManager(manager);
      return getCount(categoryMap);
    }).register(registry);
  }

  private static int getCount(Map<Long, List<Category>> categoryMap) {
    if (categoryMap == null) {
      return 0;
    }

    return categoryMap.values().stream()
        .mapToInt(List::size)
        .sum();
  }

  @Bean
  public MeterBinder categoryMapCacheSize(CacheManager cacheManager) {
    return registry -> Gauge.builder("kyumall.cache.categoryGroupByParentMap.size", cacheManager, manager -> {
      Map<Long, List<Category>> categoryMap = extractValueFromCacheManager(manager);
      int size = getCount(categoryMap);
      if (size == 0) {
        return 0;
      }

      Category firstCategory = categoryMap.values().stream()
          .findFirst().orElseGet(ArrayList::new).get(0);
      return size * calculateObjectSize(firstCategory);
    }).register(registry);
  }

  public static long calculateObjectSize(Object obj) {
    try {
      try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
          objectOutputStream.writeObject(obj);
          objectOutputStream.flush();
          return byteOutputStream.size();
        }
      }
    } catch (IOException e) {
      return 0;
    }
  }

  // Caffeine Cache는 estimateSize() 메서드를 제공해서 직접 캐시에 꺼내지 않아도 사이즈를 확인할 수 있는 반면, Jcache는 캐시를 직접 꺼내지 않고 사이즈를 예측하지 못해서 해당 기능을 사용할 수 없습니다.
  private Map<Long, List<Category>> extractValueFromCacheManager(CacheManager manager) {
    Cache cache = manager.getCache("categoryGroupByParentMap");
    com.github.benmanes.caffeine.cache.Cache caffeineCache = (com.github.benmanes.caffeine.cache.Cache)cache.getNativeCache();
    long estimatedSize = caffeineCache.estimatedSize();
    if (estimatedSize > 0) {
      ValueWrapper valueWrapper = cache.get("findCategoryGroupingByParent");
      if (valueWrapper != null) {
        return (Map<Long, List<Category>>) valueWrapper.get();
      }
    }
    return null;
  }
}
