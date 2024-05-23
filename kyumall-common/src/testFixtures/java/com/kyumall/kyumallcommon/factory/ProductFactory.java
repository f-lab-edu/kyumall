package com.kyumall.kyumallcommon.factory;

import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
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

  public Category createCategory(String name, Category parent, CategoryStatus status) {
    return categoryRepository.save(Category.builder()
            .name(name)
            .parent(parent)
            .status(status)
        .build());
  }

  public Category createCategory(String name, Category parent) {
    return categoryRepository.save(Category.builder()
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build());
  }

  public Category createCategory(String name) {
    return categoryRepository.save(Category.builder()
        .name(name)
        .status(CategoryStatus.INUSE)
        .build());
  }

  public Product createProduct(Category category, Member seller, String name, Integer price, Image image, String detail) {
    return productRepository.save(Product.builder()
            .category(category)
            .seller(seller)
            .name(name)
            .price(price)
            .image(image.getStoredFileName())
            .detail(detail)
        .build());
  }

  public Product createProduct(Category category, String name, Integer price) {
    return productRepository.save(Product.builder()
        .category(category)
        .seller(memberFactory.createMember(MemberFixture.LEE))
        .name(name)
        .price(price)
        .image(createImage().getStoredFileName())
        .detail("detail")
        .build());
  }

  public Product createProduct(String name, Integer price) {
    return productRepository.save(Product.builder()
        .category(createCategory("test"))
        .seller(memberFactory.createMember(MemberFixture.LEE))
        .name(name)
        .price(price)
        .image(createImage().getStoredFileName())
        .detail("detail")
        .build());
  }

  private Image createImage() {
    return imageRepository.save(Image.builder()
            .storedFileName("test.png")
            .storedFileName("ddd-ddd-ddd")
        .build());
  }
}
