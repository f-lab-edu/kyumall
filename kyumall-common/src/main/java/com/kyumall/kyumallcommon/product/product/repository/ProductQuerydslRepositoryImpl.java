package com.kyumall.kyumallcommon.product.product.repository;


import static com.kyumall.kyumallcommon.product.product.entity.QProduct.*;

import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductQuerydslRepositoryImpl implements ProductQuerydslRepository {
  private final JPAQueryFactory queryFactory;

  @Override
  public List<Product> search() {
    return null;
  }

  @Override
  public Optional<Product> searchById(Long id) {
    return Optional.of(queryFactory.selectFrom(product)
        .where(product.id.eq(id))
        .fetchFirst());
  }
}
