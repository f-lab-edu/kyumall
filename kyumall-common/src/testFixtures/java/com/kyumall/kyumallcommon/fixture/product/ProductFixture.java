package com.kyumall.kyumallcommon.fixture.product;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.product.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductFixture {

  APPLE(1L,"얼음골 사과", CategoryFixture.APPLE_PEAR.toEntity(), 4000, "<h1>맛있는 사과</h1>"),
  BEEF(2L,"맛있는 소고기", CategoryFixture.MEET.toEntity(), 5000, "<h1>맛있는 소고기</h1>"),

  SWEATER(3L, "가을 스웨터", CategoryFixture.CLOTHES.toEntity(), 10000, "<h1>스웨터</h1>"),
  JEANS(4L, "데님 청바지", CategoryFixture.CLOTHES.toEntity(), 5000, "<h1>청바지/h1>"),
  ;

  private final Long id;
  private final String name;
  private final Category category;
  private final int price;
  private final String detail;

  public Product toEntity(Member seller, Category category) {
    return Product.builder()
        .id(id)
        .name(name)
        .category(category)
        .price(price)
        .detail(detail)
        .seller(seller)
        .build();
  }
}
