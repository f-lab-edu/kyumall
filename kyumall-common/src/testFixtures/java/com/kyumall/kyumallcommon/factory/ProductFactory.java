package com.kyumall.kyumallcommon.factory;

import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import com.kyumall.kyumallcommon.upload.entity.Image;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductFactory {
  @Autowired
  ProductRepository productRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  MemberFactory memberFactory;
  @Autowired
  private ImageRepository imageRepository;

  public Category saveCategory(Category category) {
    return categoryRepository.save(category);
  }

  public Category createCategory(String name) {
    return categoryRepository.save(Category.builder()
        .name(name)
        .status(CategoryStatus.INUSE)
        .build());
  }

  public Category createCategory(CategoryFixture categoryFixture) {
    return saveCategoryRecursive(categoryFixture.toEntity());
  }

  private Category saveCategoryRecursive(Category category) {
    Category parentCategory = null;
    if (category.getParent() != null) {   // 부모 객체가 존재하면
      parentCategory = saveCategoryRecursive(category.getParent()); // 재귀 호출 (최상위 부모 객체 부터 저장)
    }
    return categoryRepository.saveAndFlush(category);
  }

  public Product createProduct(ProductFixture productFixture, Member seller) {
    Category category = saveCategoryRecursive(productFixture.getCategory());
    return productRepository.saveAndFlush(productFixture.toEntity(seller, category));
  }

  private Image createImage() {
    return imageRepository.save(Image.builder()
            .storedFileName("test.png")
            .storedFileName("ddd-ddd-ddd")
        .build());
  }
}
