package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {

  @EntityGraph(attributePaths = {"member", "product"})
  Optional<ProductComment> findWithProductAndMemberById(Long id);

  @EntityGraph(attributePaths = {"member"})
  Slice<ProductComment> findByProductOrderByCreatedAt(Product product, Pageable pageable);

  List<ProductComment> findByParentComment(ProductComment productComment);
}
