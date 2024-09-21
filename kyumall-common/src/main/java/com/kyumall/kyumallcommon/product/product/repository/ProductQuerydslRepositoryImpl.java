package com.kyumall.kyumallcommon.product.product.repository;


import static com.kyumall.kyumallcommon.product.product.entity.QProduct.*;

import com.kyumall.kyumallcommon.product.product.dto.ProductSearchDto;
import com.kyumall.kyumallcommon.product.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.product.product.dto.QProductSearchDto;
import com.kyumall.kyumallcommon.product.product.dto.SearchProductCondition;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.ProductStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductQuerydslRepositoryImpl implements ProductQuerydslRepository {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<ProductSearchDto> search(SearchProductCondition cond) {
    return queryFactory.select(new QProductSearchDto(
        product.id, product.name, product.seller.id, product.seller.username,
            product.productStatus, product.price
        ))
        .from(product)
        .where(
            productNameContains(cond.getName()),
            sellerIdEq(cond.getSellerId()),
            productStatusEq(cond.getProductStatusList()),
            priceLt(cond.getMaxPrice()),
            priceGoe(cond.getMinPrice()))
        .orderBy(product.createdAt.desc())
        .fetch();
  }

  private BooleanExpression productNameContains(String productName) {
    return productName != null ? product.name.contains(productName) : null;
  }

  private BooleanExpression sellerIdEq(Long sellerId) {
    return sellerId != null ? product.seller.id.eq(sellerId) : null;
  }

  private BooleanExpression productStatusEq(List<ProductStatus> productStatusList) {
    if (productStatusList == null || productStatusList.isEmpty()) {
      return null;
    }
    return product.productStatus.in(productStatusList);
  }

  private BooleanExpression priceLt(Integer maxPrice) {
    return maxPrice != null ? product.price.lt(maxPrice) : null;
  }

  private BooleanExpression priceGoe(Integer minPrice) {
    return minPrice != null ? product.price.goe(minPrice) : null;
  }

  @Override
  public Optional<Product> searchById(Long id) {
    return Optional.of(queryFactory.selectFrom(product)
        .where(product.id.eq(id))
        .fetchFirst());
  }
}
