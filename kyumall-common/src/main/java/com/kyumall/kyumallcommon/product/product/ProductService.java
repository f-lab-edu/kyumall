package com.kyumall.kyumallcommon.product.product;

import com.kyumall.kyumallcommon.product.category.dto.CategoryDto;
import com.kyumall.kyumallcommon.product.product.dto.ProductDetailDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.product.product.dto.ProductForm;
import com.kyumall.kyumallcommon.product.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.category.CategoryMapService;
import com.kyumall.kyumallcommon.product.product.dto.UpdateProductImageInfo;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.ProductImage;
import com.kyumall.kyumallcommon.product.product.entity.ProductStatus;
import com.kyumall.kyumallcommon.product.product.repository.ProductImageRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.upload.ImageUploadService;
import com.kyumall.kyumallcommon.upload.entity.Image;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ProductService {
  private final CategoryMapService categoryMapService;
  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;
  private final ProductImageRepository productImageRepository;
  private final ImageUploadService imageUploadService;

  /**
   * 상품을 생성합니다.
   * @param request
   * @param multipartImages
   * @param loginUserId
   * @return
   */
  @Transactional
  public Long createProduct(ProductForm request, List<MultipartFile> multipartImages, Long loginUserId) {
    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));

    Member seller = memberRepository.findById(loginUserId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));

    Product product = productRepository.save(Product.builder()
        .name(request.getProductName())
        .category(category)
        .seller(seller)
        .price(request.getPrice())
        .detail(request.getDetail())
        .productStatus(ProductStatus.INUSE)
        .build());

    // 이미지 업로드
    if (multipartImages != null) {
      IntStream.range(0, multipartImages.size()).forEachOrdered(idx -> {
        MultipartFile multipartImage = multipartImages.get(idx);
        Image image = imageUploadService.uploadImage(multipartImage);
        productImageRepository.save(new ProductImage(product, image, idx));
      });
    }

    return product.getId();
  }

  /**
   * 상품 정보를 수정합니다.
   * 상품을 등록한 관리자만 수정할 수 있습니다.
   * @param id
   * @param productForm 상품 정보
   * @param imageInfos  이미지정보 리스트, List 인덱스가 이미지의 순서(sequence)가 됩니다.
   * @param newMultipartImages 신규 이미지 MultipartFile 리스트
   * @param loginUserId
   */
  @Transactional
  public void updateProduct(Long id, @Valid ProductForm productForm, List<UpdateProductImageInfo> imageInfos,
      List<MultipartFile> newMultipartImages, Long loginUserId) {
    Product product = productRepository.findWithImagesById(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS));

    if (!product.isSeller(loginUserId)) {
      throw new KyumallException(ErrorCode.PRODUCT_UPDATE_FORBIDDEN);
    }
    // 카테고리 수정
    if (product.isCategoryChanged(productForm.getCategoryId())) {
      Category category = categoryRepository.findById(productForm.getCategoryId())
          .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));
      product.changeCategory(category);
    }

    // 상품 정보 수정
    product.changeInfo(productForm.getProductName(), productForm.getPrice(), productForm.getDetail());

    // 제거된 이미지 삭제 처리
    deleteRemovedImage(imageInfos, product);

    // 신규 이미지 업로드
    Map<String, Image> newImageMap = new HashMap<>();   // 기존이미지명 : 이미지객체
    for (MultipartFile newMultipartImage: newMultipartImages) {
      Image newImage = imageUploadService.uploadImage(newMultipartImage);
      newImageMap.put(newImage.getOriginalFileName(), newImage);
    }

    // 이미지 리스트 저장 (기존 이미지는 순서 변경, 신규 이미지는 insert)
    saveProductImageList(imageInfos, newImageMap, product);
  }

  private void saveProductImageList(List<UpdateProductImageInfo> imageInfos, Map<String, Image> newImageMap, Product product) {
    IntStream.range(0, imageInfos.size()).forEachOrdered(idx -> {
      UpdateProductImageInfo imageInfo = imageInfos.get(idx);
      if (imageInfo.isNew()) {    // 신규 이미지 이면 newImageMap 에서 찾아서 save (insert)
        Image newImage = newImageMap.get(imageInfo.getNewImageFileName());
        if (newImage == null) {
          throw new KyumallException(ErrorCode.IMAGE_NAME_NOT_EXISTS);
        }
        productImageRepository.save(new ProductImage(product, newImage, idx));
      } else {
        // 기존이미지면 순서만 변경해서 save (update)
        productImageRepository.save(new ProductImage(product, Image.from(imageInfo.getImageId()), idx));
      }
    });
  }

  private void deleteRemovedImage(List<UpdateProductImageInfo> imageInfos, Product product) {
    Set<String> existingImageIds = imageInfos.stream()
        .filter(imageInfo -> imageInfo.getImageId() != null)
        .map(UpdateProductImageInfo::getImageId)
        .collect(Collectors.toSet());

    product.getProductImages().stream()
        .filter(productImage -> !existingImageIds.contains(productImage.getImage().getId()))
        .forEach(this::deleteProductImage);
  }

  private void deleteProductImage(ProductImage productImage) {
    productImageRepository.delete(productImage);
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
