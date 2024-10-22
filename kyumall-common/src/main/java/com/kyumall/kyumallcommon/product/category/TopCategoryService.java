package com.kyumall.kyumallcommon.product.category;

import com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 최상위 카테고리 캐시를 위한 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopCategoryService {
  private static final String TOP_CATEGORY_CACHE = "topCategoryCache";
  private final CategoryRepository categoryRepository;

  @Cacheable(value = TOP_CATEGORY_CACHE, key = "#root.methodName")
  public List<SubCategoryDto> findTopCategory() {
    return categoryRepository.findTopSubCategoriesUsingSubquery();
  }

  @CacheEvict(value = TOP_CATEGORY_CACHE, allEntries = true)
  public void evictTopCategory() {
    log.info("topCategoryCache evicted");
  }
}
