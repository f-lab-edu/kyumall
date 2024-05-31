package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.CategoryDto;
import com.kyumall.kyumallclient.product.dto.SubCategoryDto;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {
  private final CategoryMapService categoryMapService;
  private final CategoryRepository categoryRepository;

  /**
   * 전체 카테고리를 계층형 리스트 형태로 조회
   * @return
   */
  public List<CategoryDto> getAllCategoriesHierarchy() {
    Map<Long, List<Category>> categoryGroupingByParent = categoryMapService.findCategoryGroupingByParent();
    return convertToCategoryHierarchy(categoryGroupingByParent);
  }

  /**
   * 전체 카테고리를 맵 형태로 조회
   * @return
   */
  public Map<Long, List<SubCategoryDto>> getAllCategoriesMap() {
    Map<Long, List<Category>> categoryGroupingByParent = categoryMapService.findCategoryGroupingByParent();
    return categoryGroupingByParent.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,  // 키는 변경 없이 유지
            entry -> entry.getValue().stream()
                .map(category -> SubCategoryDto.from(category, categoryGroupingByParent.containsKey(category.getId())))
                .collect(Collectors.toList())
        ));
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

  /**
   * 서브 카테고리를 조회합니다.
   * 한단계 아래 자식 서브 카테고리만 조회합니다.
   * @param id
   * @return
   */
  public List<SubCategoryDto> getOneStepSubCategories(Long id) {
    Map<Long, List<Category>> categoryGroupingByParent = categoryMapService.findCategoryGroupingByParent();
    if (!categoryGroupingByParent.containsKey(id)) {
      return new ArrayList<>();
    }
    return categoryGroupingByParent.get(id)
        .stream()
        .map(category -> SubCategoryDto.from(category, categoryGroupingByParent.containsKey(category.getId())))
        .toList();
  }
}
