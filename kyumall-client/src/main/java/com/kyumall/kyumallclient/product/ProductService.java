package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.product.dto.ProductDetailDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallclient.product.dto.CreateProductRequest;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {
  private final CategoryService categoryService;
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;

  public Long createProduct(CreateProductRequest request) {
    Category category = categoryService.findCategoryById(request.getCategoryId());

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
   * 카테고리에 해당하는 아이템을 조회합니다.
   * 입력받은 카테고리의 하위 카테고리에 해당하는 상품까지 조회합니다.
   * ex) 입력받은 카테고리 : 식품 -> 과일, 육류 카테고리의 상품까지 조회됨
   * @param categoryId
   * @param pageable
   * @return
   */
  public Slice<ProductSimpleDto> getProductsInCategory(Long categoryId, Pageable pageable) {
    List<Long> subCategoryIds;
    subCategoryIds = findSubCategories(categoryId);
    subCategoryIds.add(categoryId);
    return productRepository.findByCategoryIds(subCategoryIds, pageable).map(ProductSimpleDto::from);
  }

  /**
   * 주어진 카테고리의 하위 카테고리를 조회합니다.
   * 캐시에 저장된 categoryMap 에서 categoryId 의 서브 카테고리를 조회합니다.
   * 재귀적으로 조회
   * @param categoryId
   * @return
   */
  private List<Long> findSubCategories(Long categoryId) {
    Map<Long, List<Category>> categoryGroupingByParent = categoryService.findCategoryGroupingByParent();
    if(!categoryGroupingByParent.containsKey(categoryId)) {
      throw new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS);
    }
    List<Category> categories = categoryGroupingByParent.get(categoryId);
    List<Long> allSubCategories = new ArrayList<>();
    recursiveSetSubCategories(categories, categoryGroupingByParent, allSubCategories);
    return allSubCategories;
  }

  void recursiveSetSubCategories(List<Category> categories, Map<Long, List<Category>> categoryMap, List<Long> allSubCategories) {
    allSubCategories.addAll(categories.stream().map(Category::getId).toList());
    categories.stream().forEach(category -> {
      List<Category> subCategories = categoryMap.getOrDefault(category.getId(), new ArrayList<>());
      recursiveSetSubCategories(subCategories, categoryMap, allSubCategories);
    });
  }

  public ProductDetailDto getProduct(Long id) {
    return ProductDetailDto.from(productRepository.findWithSellerById(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS)));
  }
}
