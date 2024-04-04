package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.dto.ProductCommentCountDto;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.entity.ProductCommentRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductCommentRatingRepository extends JpaRepository<ProductCommentRating, Long> {
  Optional<ProductCommentRating> findByProductCommentAndMember(ProductComment productComment, Member member);
  Optional<ProductCommentRating> findByProductComment_IdAndMember_Id(Long productId, Long memberId);

  @Query(
      "select new com.kyumall.kyumallcommon.product.dto.ProductCommentCountDto"
          + "(pcr.productComment.id"
          + ", sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.vo.RatingType.LIKE then 1 else 0 end)"
          + ", sum(case when pcr.ratingType = com.kyumall.kyumallcommon.product.vo.RatingType.DISLIKE then 1 else 0 end)"
          + ", (sum(case when pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.vo.RatingType.LIKE then 1 else 0 end) > 0)"
          + ", (sum(case when pcr.member.id = :memberId and pcr.ratingType = com.kyumall.kyumallcommon.product.vo.RatingType.DISLIKE then 1 else 0 end) > 0)"
          + ")"
          + " from ProductCommentRating pcr "
          + " where pcr.productComment.id in :productCommentIds "
          + " group by pcr.productComment.id"
  )
  List<ProductCommentCountDto> findRatingCountInCommentIds(List<Long> productCommentIds, Long memberId);
}
