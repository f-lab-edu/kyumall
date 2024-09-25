package com.kyumall.kyumallcommon.product.product.repository;

import com.kyumall.kyumallcommon.product.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQuerydslRepository {
  @EntityGraph(attributePaths = {"category"})
  Page<Product> findAllByOrderByName(Pageable pageable);

  @Query("""
    select distinct p
    from Product p
    join fetch p.category
    left join fetch p.productImages
    order by p.createdAt desc
  """)
  Page<Product> findAllWithImagesByCreatedAtDesc(Pageable pageable);

  @Query("select p from Product p where p.category.id in :parentIds")
  Slice<Product> findByCategoryIds(List<Long> parentIds, Pageable pageable);

  @EntityGraph(attributePaths = {"seller"})
  Optional<Product> findWithSellerById(Long id);

  @EntityGraph(attributePaths = {"category", "seller"})
  Optional<Product> findWithFetchById(Long id);

  @Query("""
  select p from Product p
    join fetch p.category
    join fetch p.seller
    left join fetch p.productImages
  where p.id = :id
  """)
  Optional<Product> findWithFetchAllById(Long id);

  List<Product> findByIdIn(List<Long> productIds);

  @Query("select distinct p from Product p left join fetch p.productImages")
  Optional<Product> findWithImagesById(Long id);

}
