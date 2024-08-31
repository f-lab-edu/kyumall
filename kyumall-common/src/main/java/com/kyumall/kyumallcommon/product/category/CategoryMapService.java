package com.kyumall.kyumallcommon.product.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryMapService {
  private static final String CATEGORY_GROUP_BY_PARENT_MAP = "categoryGroupByParentMap";
  private final CategoryRepository categoryRepository;
  /**
   * 전체 카테고리를 조회하여 parentId 로 group by 한 Map 을 만듭니다.
   * 캐시는 히트율이 중요하기 때문에 id 별로 카테고리를 캐시하는 것이 아닌, 전체 카테고리를 캐시하였습니다.
   * @return all category grouping by parent id
   */
  @Cacheable(value = CATEGORY_GROUP_BY_PARENT_MAP, key = "#root.methodName")
  public Map<Long, List<Category>> findCategoryGroupingByParent() {
    log.info("findCategoryGroupingByParent not cached");
    List<Category> allCategory = categoryRepository.findAllByStatus(CategoryStatus.INUSE);
    return allCategory.stream().collect(Collectors.groupingBy(Category::getParentId));
  }

  @CacheEvict(value = CATEGORY_GROUP_BY_PARENT_MAP, allEntries = true)
  public void evictCategoryMapCache() {

  }
}
