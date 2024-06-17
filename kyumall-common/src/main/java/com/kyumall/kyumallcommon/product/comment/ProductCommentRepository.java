package com.kyumall.kyumallcommon.product.comment;

import com.kyumall.kyumallcommon.product.comment.dto.ProductCommentDto;
import com.kyumall.kyumallcommon.product.comment.dto.ReplyCountDto;
import com.kyumall.kyumallcommon.product.product.Product;
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

  @Query("""
    select pc from ProductComment pc
    join fetch pc.member
    where pc.product = :product
    and pc.parentComment is null
  """)
  Slice<ProductComment> findByProductOrderByCreatedAt(Product product, Pageable pageable);


  @Query("""
    select new com.kyumall.kyumallcommon.product.comment.dto.ProductCommentDto(
      pc.id,
      pc.member.username,
      pc.member.email,
      pc.content,
      pc.createdAt,
      (select count(1) as replyCount from ProductComment subPc where subPc.parentComment.id = pc.id),
      (select count(1) as likeCount from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.LIKE),
      (select count(1) as dislikeCount from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.DISLIKE),
      (select count(pcr) > 0 from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.LIKE),
      (select count(pcr) > 0 from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.DISLIKE)
    ) 
    from ProductComment pc
    where pc.product = :product
      and pc.parentComment is null
    order by pc.createdAt
  """)
  Slice<ProductCommentDto> findCommentDtosUsingSubquery(Product product, Long memberId, Pageable pageable);

  @EntityGraph(attributePaths = {"member"})
  Slice<ProductComment> findByParentCommentOrderByCreatedAt(ProductComment comment, Pageable pageable);

  List<ProductComment> findByParentComment(ProductComment productComment);

  @Query("select new com.kyumall.kyumallcommon.product.comment.dto.ReplyCountDto"
      + "(pc.parentComment.id, count(1)) "
      + " from ProductComment pc "
      + " where pc.parentComment.id in :commentIds "
      + " group by pc.parentComment.id"
  )
  List<ReplyCountDto> findReplyCountInCommentId(List<Long> commentIds);
}
