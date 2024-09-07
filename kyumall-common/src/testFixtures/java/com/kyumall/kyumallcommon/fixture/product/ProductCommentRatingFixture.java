package com.kyumall.kyumallcommon.fixture.product;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.product.entity.ProductCommentRating;
import com.kyumall.kyumallcommon.product.product.entity.RatingType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCommentRatingFixture {

  LIKE(RatingType.LIKE),
  DISLIKE(RatingType.DISLIKE);

  private final RatingType ratingType;

  public ProductCommentRating toEntity(ProductComment productComment, Member member) {
    return ProductCommentRating.builder()
        .productComment(productComment)
        .member(member)
        .ratingType(ratingType)
        .build();
  }
}
