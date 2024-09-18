package com.kyumall.kyumallcommon.product.product.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageKey implements Serializable {
  private Long product;
  private String image;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProductImageKey that = (ProductImageKey) o;
    return Objects.equals(product, that.product) && Objects.equals(image,
        that.image);
  }

  @Override
  public int hashCode() {
    return Objects.hash(product, image);
  }
}
