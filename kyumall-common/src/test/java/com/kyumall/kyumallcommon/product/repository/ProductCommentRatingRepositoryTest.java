package com.kyumall.kyumallcommon.product.repository;


import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.JpaRepositoryTest;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductCommentRatingRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductCommentRepository;
import com.kyumall.kyumallcommon.product.product.dto.LikeCountDto;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.product.entity.ProductCommentRating;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.product.entity.RatingType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


class ProductCommentRatingRepositoryTest extends JpaRepositoryTest {
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  ProductRepository productRepository;
  @Autowired
  ProductCommentRepository productCommentRepository;
  @Autowired
  ProductCommentRatingRepository productCommentRatingRepository;

  List<Member> members;
  Product product1;
  ProductComment comment1;
  ProductComment comment2;

  @BeforeEach
  void initData() {
    members = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      members.add(Member.builder()
          .username("test" + i)
          .email("test"+ i +"@email.com")
          .password("12341234").build());
    }
    memberRepository.saveAllAndFlush(members);

    product1 = productRepository.saveAndFlush(Product.builder()
        .name("product1")
        .build());

    comment1 = productCommentRepository.saveAndFlush(ProductComment.builder()
        .product(product1)
        .content("댓글 입니다.")
        .build());

    comment2 = productCommentRepository.saveAndFlush(ProductComment.builder()
        .product(product1)
        .content("댓글 입니다.")
        .build());
  }

  ProductCommentRating createRating(ProductComment productComment, Member member, RatingType ratingType) {
    return productCommentRatingRepository.save(ProductCommentRating.builder()
            .productComment(productComment)
            .member(member)
            .ratingType(ratingType)
        .build());
  }

  @Test
  @DisplayName("댓글 리스트의 좋아요수, 싫어요 수 조회에 성공합니다.")
  void findRatingCountInCommentIds_success() {
    // given
    int totalCount = members.size();
    int likeCount = 18;
    saveCommentRatingList(comment1, totalCount, likeCount);
    saveCommentRatingList(comment2, totalCount, likeCount);
    List<Long> commentIds = List.of(comment1.getId(), comment2.getId());

    // when
    List<LikeCountDto> countDtos = productCommentRatingRepository.findRatingCountInCommentIds(
        commentIds, members.get(0).getId());

    // then
    System.out.println(countDtos);
    // 댓글 1
    LikeCountDto comment1Count = countDtos.stream()
        .filter(countDto -> countDto.getProductCommentId().equals(comment1.getId())).findAny().get();
    assertThat(comment1Count.getLikeCount()).isEqualTo(likeCount);
    assertThat(comment1Count.getDislikeCount()).isEqualTo(totalCount - likeCount);
    assertThat(comment1Count.isLikeByCurrentUser()).isTrue();
    assertThat(comment1Count.isDislikeByCurrentUser()).isFalse();
    // 댓글 2
    LikeCountDto comment2Count = countDtos.stream()
        .filter(countDto -> countDto.getProductCommentId().equals(comment2.getId())).findAny().get();
    assertThat(comment2Count.getLikeCount()).isEqualTo(likeCount);
    assertThat(comment2Count.getDislikeCount()).isEqualTo(totalCount - likeCount);
    assertThat(comment2Count.isLikeByCurrentUser()).isTrue();
    assertThat(comment2Count.isDislikeByCurrentUser()).isFalse();
  }

  private List<ProductCommentRating> saveCommentRatingList(ProductComment productComment , int totalCount, int likeCount) {
    List<ProductCommentRating> ratings = new ArrayList<>();
    for (int i = 0; i < likeCount; i++) {
      ratings.add(createRating(productComment, members.get(i), RatingType.LIKE));
    }
    for (int i = likeCount; i < totalCount; i++) {
      ratings.add(createRating(productComment, members.get(i), RatingType.DISLIKE));
    }
    productCommentRatingRepository.saveAllAndFlush(ratings);
    return ratings;
  }
}
