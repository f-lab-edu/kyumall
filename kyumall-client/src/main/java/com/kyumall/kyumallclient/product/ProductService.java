package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallcommon.exception.CacheNotFoundException;
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
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
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
  private final CacheManager cacheManager;

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
  @Cacheable(value = "categoryMap", key = "#root.methodName")
  public Map<Long, List<Category>> findCategoryGroupingByParent() {
    List<Category> allCategory = categoryRepository.findAllByStatus(CategoryStatus.INUSE);
    return allCategory.stream().collect(Collectors.groupingBy(Category::getParentId));
  }

  //캐시는 히트율이 중요함 id 별로 카테고리를 캐시하지 말고, 전체 카테고리를 캐시해 둘 것
  private Category findCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
  }

  /**
   * 카테고리에 해당하는 아이템을 조회합니다.
   * 입력받은 카테고리의 하위 카테고리에 해당하는 상품까지 조회합니다.
   * ex) 입력받은 카테고리 : 식품 -> 과일, 육류 카테고리의 상품까지 조회됨
   * @param categoryId
   * @param pageable
   * @return
   */
  public Slice<ProductSimpleDto> getProductsInCategory(Long categoryId, Pageable pageable) {
    List<Long> subCategoryIds;
    try {
      subCategoryIds = getSubCategoriesFromCache(categoryId);
    } catch (CacheNotFoundException e) {
      subCategoryIds = categoryRepository.findSubCategoryIds(categoryId);
    }
    subCategoryIds.add(categoryId);
    return productRepository.findByCategoryIds(subCategoryIds, pageable).map(ProductSimpleDto::from);
  }

  /**
   * 캐시에 저장된 categoryMap 에서 categoryId 의 서브 카테고리를 조회합니다.
   * 재귀적으로 조회
   * @param categoryId
   * @return
   */
  private List<Long> getSubCategoriesFromCache(Long categoryId) throws CacheNotFoundException {
    Cache categoryMapCache = cacheManager.getCache("categoryMap");
    if (categoryMapCache != null) {
      ValueWrapper valueWrapper = categoryMapCache.get("findCategoryGroupingByParent");
      if (valueWrapper != null) {
        Map<Long, List<Category>> categoryMap = (Map<Long, List<Category>>)valueWrapper.get();
        List<Category> categories = categoryMap.get(categoryId);
        List<Long> allSubCategories = new ArrayList<>();
        recursiveSetSubCategories(categories, categoryMap, allSubCategories);
        return allSubCategories;
      }
    }
    throw new CacheNotFoundException("categoryMap 캐시를 찾을 수 없습니다.");
  }

  void recursiveSetSubCategories(List<Category> categories, Map<Long, List<Category>> categoryMap, List<Long> allSubCategories) {
    allSubCategories.addAll(categories.stream().map(Category::getId).toList());
    categories.stream().forEach(category -> {
      List<Category> subCategories = categoryMap.getOrDefault(category.getId(), new ArrayList<>());
      recursiveSetSubCategories(subCategories, categoryMap, allSubCategories);
    });
  }
}
