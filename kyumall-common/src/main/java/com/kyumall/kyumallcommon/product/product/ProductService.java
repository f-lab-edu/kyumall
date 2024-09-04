package com.kyumall.kyumallcommon.product.product;

import com.kyumall.kyumallcommon.product.category.dto.CategoryDto;
import com.kyumall.kyumallcommon.product.product.dto.ProductDetailDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.product.product.dto.CreateProductRequest;
import com.kyumall.kyumallcommon.product.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.category.CategoryMapService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {
  private final CategoryMapService categoryMapService;
  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;

  /**
   * 상품을 생성합니다.
   * @param request
   * @return
   */
  public Long createProduct(CreateProductRequest request) {
    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));

    Member seller = memberRepository.findByUsername(request.getSellerUsername())
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));

    Product product = productRepository.save(Product.builder()
        .name(request.getProductName())
        .category(category)
        .seller(seller)
        .price(request.getPrice())
        .detail(request.getDetail())
        .productStatus(ProductStatus.INUSE)
        .build());
    return product.getId();
  }

  /**
   * 모든 상품을 조회합니다.
   * @param pageable
   * @return
   */
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
    List<String> subCategoryIds = findAllSubCategories(categoryId);
    List<Long> longIdList = subCategoryIds.stream().map(Long::parseLong).collect(Collectors.toList());
    return productRepository.findByCategoryIds(longIdList, pageable).map(ProductSimpleDto::from);
  }

  /**
   * 주어진 카테고리의 '모든' 하위 카테고리를 조회합니다.
   * 캐시에 저장된 categoryMap 에서 categoryId 의 하위 카테고리를 조회하고, 그 하위 카테고리의 하위 카테고리를 재귀 형식으로 조회하여 모든 하위 카테고리를 조회합니다.
   * 재귀적으로 조회
   * @param categoryId
   * @return
   */
  private List<String> findAllSubCategories(Long categoryId) {
    Map<String, List<CategoryDto>> categoryMap = categoryMapService.findCategoryGroupingByParent();
    if(!categoryMap.containsKey(categoryId.toString())) {
      throw new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS);
    }
    List<CategoryDto> subCategories = categoryMap.get(categoryId.toString());
    List<String> allSubCategories = new ArrayList<>();
    allSubCategories.add(categoryId.toString()); // 입력받은 카테고리 추가
    recursiveSetSubCategories(subCategories, categoryMap, allSubCategories);
    return allSubCategories;
  }

  /**
   * 재귀적으로 호출하여 모든 하위 카테고리를 세팅합니다.
   * @param categories
   * @param categoryMap
   * @param allSubCategories
   */
  private void recursiveSetSubCategories(List<CategoryDto> categories, Map<String, List<CategoryDto>> categoryMap, List<String> allSubCategories) {
    allSubCategories.addAll(categories.stream().map(CategoryDto::getId).toList());
    categories.stream().forEach(categoryDto -> {
      List<CategoryDto> subCategories = categoryMap.getOrDefault(categoryDto.getId(), new ArrayList<>());
      recursiveSetSubCategories(subCategories, categoryMap, allSubCategories);
    });
  }

  /**
   * id에 해당하는 상품을 조회합니다.
   * @param id
   * @return
   */
  public ProductDetailDto getProduct(Long id) {
    return ProductDetailDto.from(productRepository.findWithSellerById(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS)));
  }
}
