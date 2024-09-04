package com.kyumall.kyumallclient.product.category;

import com.kyumall.kyumallclient.product.category.dto.HierarchyCategoryDto;
import com.kyumall.kyumallclient.product.category.dto.SubCategoryDto;
import com.kyumall.kyumallcommon.product.category.dto.CategoryDto;
import com.kyumall.kyumallcommon.product.category.dto.CreateCategoryRequest;
import com.kyumall.kyumallcommon.product.category.CategoryMapService;
import com.kyumall.kyumallcommon.product.category.CategoryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryFacade {
  private final CategoryMapService categoryMapService;
  private final CategoryService categoryService;

  /**
   * 전체 카테고리를 계층형 리스트 형태로 조회
   * @return
   */
  public List<HierarchyCategoryDto> getAllCategoriesHierarchy() {
    Map<String, List<CategoryDto>> categoryGroupingByParent = categoryMapService.findCategoryGroupingByParent();
    return convertToCategoryHierarchy(categoryGroupingByParent);
  }

  /**
   * 전체 카테고리를 맵 형태로 조회
   * @return
   */
  public Map<Long, List<SubCategoryDto>> getAllCategoriesMap() {
    Map<String, List<CategoryDto>> categoryGroupingByParent = categoryMapService.findCategoryGroupingByParent();
    return categoryGroupingByParent.entrySet().stream()
        .collect(Collectors.toMap(
            entry -> Long.parseLong(entry.getKey()),  // 키는 변경 없이 유지
            entry -> entry.getValue().stream()
                .map(categoryDto -> SubCategoryDto.from(categoryDto, categoryGroupingByParent.containsKey(categoryDto.getId())))
                .collect(Collectors.toList())
        ));
  }


  /**
   * 카테고리 Map을 계층형 구조의 List로 변경
   * @param groupingByParentIdMap
   * @return
   */
  private List<HierarchyCategoryDto> convertToCategoryHierarchy(Map<String, List<CategoryDto>> groupingByParentIdMap) {
    List<HierarchyCategoryDto> rootCategories = groupingByParentIdMap.get("0")  // root 카테고리는 parentId가 0이다.
        .stream().map(HierarchyCategoryDto::from)
        .collect(Collectors.toList());
    addSubCategories(rootCategories, groupingByParentIdMap);
    return rootCategories;
  }

  /**
   * 재귀 호출로 카테고리의 서브 카테고리를 계층형으로 추가합니다.
   * @param hierarchyCategoryDtos
   * @param categoryMap
   */
  private void addSubCategories(List<HierarchyCategoryDto> hierarchyCategoryDtos, Map<String, List<CategoryDto>> categoryMap) {
    hierarchyCategoryDtos.stream().forEach(
        hierarchyCategoryDto -> {
          List<HierarchyCategoryDto> subCategories = categoryMap.getOrDefault(
                  hierarchyCategoryDto.getId().toString(), new ArrayList<>())
              .stream().map(HierarchyCategoryDto::from).toList();
          hierarchyCategoryDto.setSubCategories(subCategories);
          addSubCategories(subCategories, categoryMap);
        }
    );
  }

  /**
   * 서브 카테고리를 조회합니다.
   * 한단계 아래 자식 서브 카테고리만 조회합니다.
   * @param id
   * @return
   */
  public List<SubCategoryDto> getOneStepSubCategories(String id) {
    Map<String, List<CategoryDto>> categoryGroupingByParent = categoryMapService.findCategoryGroupingByParent();
    if (!categoryGroupingByParent.containsKey(id)) {
      return new ArrayList<>();
    }
    return categoryGroupingByParent.get(id)
        .stream()
        .map(category -> SubCategoryDto.from(category, categoryGroupingByParent.containsKey(category.getId())))
        .toList();
  }

  public void evictCategoryCache() {
    categoryMapService.evictCategoryMapCache();
  }

  public Long createdCategory(CreateCategoryRequest request) {
    categoryMapService.evictCategoryMapCache();
    return categoryService.createCategory(request).getId();
  }
}
