package com.kyumall.kyumallcommon.product.product.repository;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.product.dto.LikeCountDto;
import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.product.entity.ProductCommentRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductCommentRatingRepository extends JpaRepository<ProductCommentRating, Long> {
  Optional<ProductCommentRating> findByProductCommentAndMember(ProductComment productComment, Member member);
  Optional<ProductCommentRating> findByProductComment_IdAndMember_Id(Long productId, Long memberId);

  @Query(
      "select new com.kyumall.kyumallcommon.product.product.dto.LikeCountDto"
          + "(pcr.productComment.id"
          + ", sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.LIKE then 1 else 0 end)"
          + ", sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.DISLIKE then 1 else 0 end)"
          + ", (sum(case when pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.LIKE then 1 else 0 end) > 0)"
          + ", (sum(case when pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.product.entity.RatingType.DISLIKE then 1 else 0 end) > 0)"
          + ")"
          + " from ProductCommentRating pcr "
          + " where pcr.productComment.id in :productCommentIds "
          + " group by pcr.productComment.id"
  )
  List<LikeCountDto> findRatingCountInCommentIds(List<Long> productCommentIds, Long memberId);
}
