package com.kyumall.kyumallcommon.product.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 상품 추가/수정 양식
 */
@Getter @AllArgsConstructor @Builder
public class ProductForm {
  @NotEmpty
  private String productName;
  @NotNull
  private Long categoryId;
  private String sellerUsername;
  @NotNull @Min(0)
  private Integer price;
  private String detail;
}
