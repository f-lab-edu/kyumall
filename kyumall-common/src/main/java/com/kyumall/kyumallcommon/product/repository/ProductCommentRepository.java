package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.dto.ReplyCountDto;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {

  @EntityGraph(attributePaths = {"member", "product"})
  Optional<ProductComment> findWithProductAndMemberById(Long id);

  @EntityGraph(attributePaths = {"member"})
  Slice<ProductComment> findByProductOrderByCreatedAt(Product product, Pageable pageable);

  List<ProductComment> findByParentComment(ProductComment productComment);

  @Query("select new com.kyumall.kyumallcommon.product.dto.ReplyCountDto"
      + "(pc.parentComment.id, count(1)) "
      + " from ProductComment pc "
      + " where pc.parentComment.id in :commentIds "
      + " group by pc.parentComment.id"
  )
  List<ReplyCountDto> findReplyCountInCommentId(List<Long> commentIds);
}
