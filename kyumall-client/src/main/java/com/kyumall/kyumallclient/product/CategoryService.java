package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  //캐시는 히트율이 중요함 id 별로 카테고리를 캐시하지 말고, 전체 카테고리를 캐시해 둘 것
  public Category findCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
  }

  /**
   * 전체 카테고리를 조회하여 parentId 로 group by 한 Map 을 만듭니다.
   * @return all category grouping by parent id
   */
  @Cacheable(value = "categoryMap", key = "#root.methodName")
  public Map<Long, List<Category>> findCategoryGroupingByParent() {
    List<Category> allCategory = categoryRepository.findAllByStatus(CategoryStatus.INUSE);
    return allCategory.stream().collect(Collectors.groupingBy(Category::getParentId));
  }
}
