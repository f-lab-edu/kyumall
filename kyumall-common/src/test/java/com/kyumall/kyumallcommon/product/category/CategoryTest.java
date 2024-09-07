package com.kyumall.kyumallcommon.product.category;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void isParentChanged_변경된경우_success() {
    Category food = CategoryFixture.FOOD.toEntity();
    Category fruit = CategoryFixture.FRUIT.toEntity(food);

    boolean isChanged = fruit.isParentChanged(CategoryFixture.CLOTHES.getId());

    assertThat(isChanged).isTrue();
  }

  @Test
  void isParentChanged_변경안된경우_success() {
    Category food = CategoryFixture.FOOD.toEntity();
    Category fruit = CategoryFixture.FRUIT.toEntity(food);

    boolean isChanged = fruit.isParentChanged(CategoryFixture.FOOD.getId());

    assertThat(isChanged).isFalse();
  }
}
