package com.kyumall.kyumallcommon.product.product.repository;

import com.kyumall.kyumallcommon.product.product.dto.ProductCommentDto;
import com.kyumall.kyumallcommon.product.product.dto.ReplyCountDto;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
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
    select new com.kyumall.kyumallcommon.product.product.dto.ProductCommentDto(
      pc.id,
      pc.member.username,
      pc.member.email,
      pc.content,
      pc.createdAt,
      (select count(1) as replyCount from ProductComment subPc where subPc.parentComment.id = pc.id),
      (select count(1) as likeCount from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.LIKE),
      (select count(1) as dislikeCount from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.DISLIKE),
      (select count(pcr) > 0 from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.LIKE),
      (select count(pcr) > 0 from ProductCommentRating pcr 
        where pcr.productComment.id = pc.id and pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.DISLIKE)
    ) 
    from ProductComment pc
    where pc.product = :product
      and pc.parentComment is null
    order by pc.createdAt
  """)
  Slice<ProductCommentDto> findCommentDtosUsingSubquery(Product product, Long memberId, Pageable pageable);

  @Query("""
    select new com.kyumall.kyumallcommon.product.product.dto.ProductCommentDto(
      pc.id,
      pc.member.username,
      pc.member.email,
      pc.content,
      pc.createdAt,
      (select count(1) as replyCount from ProductComment subPc where subPc.parentComment.id = pc.id),
      coalesce(sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.LIKE then 1 else 0 end), 0),
      coalesce(sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.DISLIKE then 1 else 0 end), 0),
      case 
        when coalesce(max(case when pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.LIKE and pcr.member.id = :memberId then 1 else 0 end), 0) = 1
        then true
        else false
      end,
      case 
        when coalesce(max(case when pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.DISLIKE and pcr.member.id = :memberId then 1 else 0 end), 0) = 1
        then true
        else false
      end
    )
    from ProductComment pc
      left join ProductCommentRating pcr
        on pcr.productComment.id = pc.id
    where pc.product = :product
      and pc.parentComment is null
    group by pc.id, pc.member.username, pc.member.email, pc.content, pc.createdAt, (select count(1) as replyCount from ProductComment subPc where subPc.parentComment.id = pc.id)
    order by pc.createdAt
  """)
  Slice<ProductCommentDto> findCommentDtosUsingJoin(Product product, Long memberId, Pageable pageable);

  @EntityGraph(attributePaths = {"member"})
  Slice<ProductComment> findByParentCommentOrderByCreatedAt(ProductComment comment, Pageable pageable);

  List<ProductComment> findByParentComment(ProductComment productComment);

  @Query("select new com.kyumall.kyumallcommon.product.product.dto.ReplyCountDto"
      + "(pc.parentComment.id, count(1)) "
      + " from ProductComment pc "
      + " where pc.parentComment.id in :commentIds "
      + " group by pc.parentComment.id"
  )
  List<ReplyCountDto> findReplyCountInCommentId(List<Long> commentIds);
}
