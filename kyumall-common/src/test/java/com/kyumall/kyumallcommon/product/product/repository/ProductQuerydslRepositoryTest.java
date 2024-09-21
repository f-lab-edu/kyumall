package com.kyumall.kyumallcommon.product.product.repository;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.JpaRepositoryTest;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.product.dto.ProductSearchDto;
import com.kyumall.kyumallcommon.product.product.dto.SearchProductCondition;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class ProductQuerydslRepositoryTest extends JpaRepositoryTest {
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductRepository productRepository;

  private Member seller;

  @BeforeEach
  void dataInit() {
    seller = memberFactory.createMember(MemberFixture.BILLY);
  }

  @Test
  @DisplayName("querydsl로 만든 메서드로 상품의 id로 상품을 조회합니다.")
  void findById_querydsl_success() {
    Product product = productFactory.createProduct(ProductFixture.APPLE, seller);

    Product foundProduct = productRepository.searchById(product.getId()).orElseThrow();

    assertThat(foundProduct.getId()).isEqualTo(product.getId());
  }

  @Test
  @DisplayName("아무 조건없이 상품을 검색하여 모든 조건의 상품을 조회합니다.")
  void search_product_by_no_condition_success() {
    List<Product> products = new ArrayList<>();
    products.add(productFactory.createProduct(ProductFixture.APPLE, seller));
    products.add(productFactory.createProduct(ProductFixture.BEEF, seller));
    products.add(productFactory.createProduct(ProductFixture.SWEATER, seller));


    log.info("------when start------");
    SearchProductCondition cond = SearchProductCondition.builder()
        .build();

    List<ProductSearchDto> searchResult = productRepository.search(cond);

    assertThat(searchResult).hasSize(3);
    assertThat(searchResult).usingRecursiveComparison().comparingOnlyFields("id")
        .isEqualTo(products);
  }

}
