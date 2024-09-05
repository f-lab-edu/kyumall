package com.kyumall.kyumallcommon.product.category;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.product.category.dto.CreateCategoryRequest;
import com.kyumall.kyumallcommon.product.category.dto.UpdateCategoryRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

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
}
