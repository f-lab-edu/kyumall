package com.kyumall.kyumallclient.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class CreateProductRequest {
  @NotEmpty
  private String productName;
  @NotNull
  private Long categoryId;
  @NotNull @Min(0)
  private Integer price;
  private String detail;
}
