package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.CategoryDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallclient.product.dto.CreateProductRequest;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;

  public Long createProduct(CreateProductRequest request) {
    Category category = findCategoryById(request.getCategoryId());

    Member seller = memberRepository.findByUsername(request.getSellerUsername())
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));

    Product product = productRepository.save(Product.builder()
        .name(request.getProductName())
        .category(category)
        .seller(seller)
        .price(request.getPrice())
        .detail(request.getDetail())
        .build());
    return product.getId();
  }

  public Page<ProductSimpleDto> getAllProducts(Pageable pageable) {
    return productRepository.findAllByOrderByName(pageable).map(ProductSimpleDto::from);
  }

  /**
   * 전체 카테고리를 조회하여 parentId 로 group by 한 Map 을 만듭니다.
   * @return all category grouping by parent id
   */
  @Cacheable("categoryMap")
  public Map<Long, List<Category>> findCategoryGroupingByParent() {
    List<Category> allCategory = categoryRepository.findAllByStatus(CategoryStatus.INUSE);
    return allCategory.stream().collect(Collectors.groupingBy(Category::getParentId));
  }

  //캐시는 히트율이 중요함
  //@Cacheable(value = "allCategories", key = "#categoryId")
  private Category findCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
  }

  public Slice<ProductSimpleDto> getProductsInCategory(Long categoryId, Pageable pageable) {
    List<Long> subCategoryIds = categoryRepository.findSubCategoryIds(categoryId);
    subCategoryIds.add(categoryId);
    return productRepository.findByCategoryIds(subCategoryIds, pageable).map(ProductSimpleDto::from);
  }
}
