package com.kyumall.kyumallcommon.factory;

import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.product.Product;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductFactory {
  @Autowired
  ProductRepository productRepository;
  @Autowired
  CategoryRepository categoryRepository;

  public Category createCategory(CategoryFixture categoryFixture) {
    return saveCategoryRecursive(categoryFixture.toEntity());
  }

  public Product createProduct(ProductFixture productFixture, Member seller) {
    Category category = saveCategoryRecursive(productFixture.getCategory());
    return productRepository.saveAndFlush(productFixture.toEntity(seller, category));
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
