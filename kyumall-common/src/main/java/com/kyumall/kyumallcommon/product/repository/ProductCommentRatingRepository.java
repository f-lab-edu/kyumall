package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.entity.ProductCommentRating;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCommentRatingRepository extends JpaRepository<ProductCommentRating, Long> {
  Optional<ProductCommentRating> findByProductCommentAndMember(ProductComment productComment, Member member);
  Optional<ProductCommentRating> findByProductComment_IdAndMember_Id(Long productId, Long MemberId);
}
