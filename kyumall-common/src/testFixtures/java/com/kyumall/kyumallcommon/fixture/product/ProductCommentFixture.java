package com.kyumall.kyumallcommon.fixture.product;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCommentFixture {
  THANKS("감사합니다."),
  GOOD("상품 좋습니다."),
  HELP("어떻게 사용하는 건가요?"),
  ;

  private final String content;

  public ProductComment toEntity(Product product, Member member, ProductComment parentComment) {
    return ProductComment.builder()
        .product(product)
        .member(member)
        .content(content)
        .parentComment(parentComment) // null 일 경우, 댓글, null이 아닐 경우 대댓글
        .build();
  }
}
