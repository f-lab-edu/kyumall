package com.kyumall.kyumallcommon.factory;

import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.product.entity.ProductImage;
import com.kyumall.kyumallcommon.product.product.repository.ProductImageRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.upload.entity.Image;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductFactory {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductImageRepository productImageRepository;
  private final ImageRepository imageRepository;

  public Category createCategory(CategoryFixture categoryFixture) {
    return saveCategoryRecursive(categoryFixture.toEntity());
  }

  public Product createProduct(ProductFixture productFixture, Member seller) {
    // 카테고리 저장
    Category category = saveCategoryRecursive(productFixture.getCategory());
    // 상품 저장
    Product product = productRepository.saveAndFlush(productFixture.toEntity(seller, category));
    // 상품이미지 있을 경우, 상품 이미지 저장
    if (productFixture.getImages() != null) {
      List<Image> images = imageRepository.saveAllAndFlush(productFixture.getImages());
      IntStream.range(0, images.size()).forEachOrdered(idx -> {
        Image image = images.get(idx);
        productImageRepository.saveAndFlush(new ProductImage(product, image, idx));
      });
    }
    return productRepository.findWithFetchAllById(product.getId()).orElseThrow();
  }

  public Product saveProduct(Product product) {
    return productRepository.saveAndFlush(product);
  }

  private Category saveCategoryRecursive(Category category) {
    Category parentCategory = null;
    if (category.getParent() != null) {   // 부모 객체가 존재하면
      parentCategory = saveCategoryRecursive(category.getParent()); // 재귀 호출 (최상위 부모 객체 부터 저장)
    }
    return categoryRepository.saveAndFlush(category);
  }
}
