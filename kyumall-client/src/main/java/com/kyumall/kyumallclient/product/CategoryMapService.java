package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryMapService {
  private final CategoryRepository categoryRepository;
  /**
   * 전체 카테고리를 조회하여 parentId 로 group by 한 Map 을 만듭니다.
   * 캐시는 히트율이 중요함 id 별로 카테고리를 캐시하지 말고, 전체 카테고리를 캐시해 둘 것
   * @return all category grouping by parent id
   */
  @Cacheable(value = "categoryMap", key = "#root.methodName")
  public Map<Long, List<Category>> findCategoryGroupingByParent() {
    List<Category> allCategory = categoryRepository.findAllByStatus(CategoryStatus.INUSE);
    return allCategory.stream().collect(Collectors.groupingBy(Category::getParentId));
  }

}
