package com.kyumall.kyumallcommon.product.category;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.product.category.dto.CategoryDto;
import com.kyumall.kyumallcommon.product.category.dto.CreateCategoryRequest;
import com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto;
import com.kyumall.kyumallcommon.product.category.dto.UpdateCategoryRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final TopCategoryService topCategoryService;

  /**
   * 카테고리를 생성합니다.
   * @param request
   * @return
   */
  public Category createCategory(CreateCategoryRequest request) {
    Category parentCategory = null;
    if (request.getParentId() != null && request.getParentId() != 0L) {
      parentCategory = categoryRepository.findById(request.getParentId())
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
    }
    return categoryRepository.save(request.toEntity(parentCategory));
  }

  /**
   * 카테고리 데이터를 수정합니다.
   * @param id
   * @param request
   * @return
   */
  @Transactional
  public void updateCategory(Long id, UpdateCategoryRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
    // 이름 변경
    category.changeName(request.getNewName());
    // 부모 카테고리 변경
    if (category.isParentChanged(request.getNewParentId())) {
      Category newParentCategory = categoryRepository.findById(request.getNewParentId())
          .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
      category.changeParent(newParentCategory);
    }
  }

  /**
   * 한단계 아래 서브 카테고리를 조회합니다.
   * 최상위 카테고리는 캐시에서 반환합니다.
   * @param id
   * @return
   */
  public List<SubCategoryDto> getOneStepSubCategories(Long id) {
    if (id == 0) {   // 최상위 카테고리
      return topCategoryService.findTopCategory();  // 캐시
//      return categoryRepository.findTopSubCategoriesUsingJoin();  // 캐시 사용 안함
    }
    return categoryRepository.findSubCategoriesUsingSubquery(id);
  }

  /**
   * 전체 카테고리를 Map 으로 Group by 하여 반환합니다.
   * @return
   */
  public Map<String, List<CategoryDto>> findCategoryGroupingByParent() {
    List<CategoryDto> allCategory = categoryRepository.findAllByStatus(CategoryStatus.INUSE)
        .stream().map(CategoryDto::from).collect(Collectors.toList());
    return allCategory.stream().collect(Collectors.groupingBy(CategoryDto::getParentId));
  }
}
