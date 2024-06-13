package com.kyumall.kyumallcommon.product.service;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.product.dto.CreateCategoryRequest;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  /**
   * 카테고리를 생성합니다.
   * parentId 가 null 이면 최상위 카테고리를 의미합니다.
   * @param request
   * @return
   */
  public Category createCategory(CreateCategoryRequest request) {
    Category parentCategory = null;
    if (request.getParentId() != null) {
      parentCategory = categoryRepository.findById(request.getParentId())
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
    }
    return categoryRepository.save(request.toEntity(parentCategory));
  }
}
