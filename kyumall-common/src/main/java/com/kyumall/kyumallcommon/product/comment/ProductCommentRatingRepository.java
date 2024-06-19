package com.kyumall.kyumallcommon.product.comment;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.comment.dto.LikeCountDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductCommentRatingRepository extends JpaRepository<ProductCommentRating, Long> {
  Optional<ProductCommentRating> findByProductCommentAndMember(ProductComment productComment, Member member);
  Optional<ProductCommentRating> findByProductComment_IdAndMember_Id(Long productId, Long memberId);

  @Query(
      "select new com.kyumall.kyumallcommon.product.comment.dto.LikeCountDto"
          + "(pcr.productComment.id"
          + ", sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.LIKE then 1 else 0 end)"
          + ", sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.DISLIKE then 1 else 0 end)"
          + ", (sum(case when pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.LIKE then 1 else 0 end) > 0)"
          + ", (sum(case when pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.comment.RatingType.DISLIKE then 1 else 0 end) > 0)"
          + ")"
          + " from ProductCommentRating pcr "
          + " where pcr.productComment.id in :productCommentIds "
          + " group by pcr.productComment.id"
  )
  List<LikeCountDto> findRatingCountInCommentIds(List<Long> productCommentIds, Long memberId);
}
