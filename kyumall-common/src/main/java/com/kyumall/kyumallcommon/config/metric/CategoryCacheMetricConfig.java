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
@Configuration
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

  private Map<Long, List<Category>> extractValueFromCacheManager(CacheManager manager) {
    Cache cache = manager.getCache("categoryGroupByParentMap");
    ValueWrapper valueWrapper = cache.get("findCategoryGroupingByParent");
    if (valueWrapper != null) {
      return (Map<Long, List<Category>>) valueWrapper.get();
    }
    return null;
  }
}
