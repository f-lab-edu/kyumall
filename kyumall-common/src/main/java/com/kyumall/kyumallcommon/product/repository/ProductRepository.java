package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.entity.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
  @EntityGraph(attributePaths = {"category"})
  Page<Product> findAllByOrderByName(Pageable pageable);

  @Query("select p from Product p where p.category.id in :parentIds")
  Slice<Product> findByCategoryIds(List<Long> parentIds, Pageable pageable);
}
