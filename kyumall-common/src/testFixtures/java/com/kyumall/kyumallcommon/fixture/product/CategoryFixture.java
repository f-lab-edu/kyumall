package com.kyumall.kyumallcommon.fixture.product;

import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CategoryFixture {
  FOOD(1L,"식품", null),
  FRUIT(2L, "과일", FOOD.toEntity()),
  MEET(3L, "육류", FOOD.toEntity()),
  APPLE_PEAR(4L ,"사과/배", FRUIT.toEntity()),

  FASHION(5L, "패션", null),
  CLOTHES(6L, "의류", FASHION.toEntity())
  ;

  private final Long id;
  private final String name;
  private final Category parent;

  public Category toEntity() {
    return Category.builder()
        .id(id)
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build();
  }

  public Category toEntity(Category parent) {
    return Category.builder()
        .id(id)
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build();
  }
}
