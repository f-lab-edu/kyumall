package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.CategoryDto;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {
  private final CategoryMapService categoryMapService;
  private final CategoryRepository categoryRepository;

  public List<CategoryDto> getAllCategories() {
    Map<Long, List<Category>> categoryGroupingByParent = categoryMapService.findCategoryGroupingByParent();
    return convertToCategoryHierarchy(categoryGroupingByParent);
  }


  /**
   * 카테고리 Map을 계층형 구조의 List로 변경
   * @param groupingByParentId
   * @return
   */
  private List<CategoryDto> convertToCategoryHierarchy(Map<Long, List<Category>> groupingByParentId) {
    List<CategoryDto> rootCategories = groupingByParentId.get(0L).stream().map(CategoryDto::from)
        .toList();
    addSubCategories(rootCategories, groupingByParentId);
    return rootCategories;
  }

  /**
   * 재귀 호출로 카테고리의 서브 카테고리를 계층형으로 추가합니다.
   * @param categoryDtos
   * @param groupingByParentId
   */
  private void addSubCategories(List<CategoryDto> categoryDtos, Map<Long, List<Category>> groupingByParentId) {
    categoryDtos.stream().forEach(
        categoryDto -> {
          List<CategoryDto> subCategories = groupingByParentId.getOrDefault(categoryDto.getId(), new ArrayList<>())
              .stream().map(CategoryDto::from).toList();
          categoryDto.setSubCategories(subCategories);
          addSubCategories(subCategories, groupingByParentId);
        }
    );
  }
}
