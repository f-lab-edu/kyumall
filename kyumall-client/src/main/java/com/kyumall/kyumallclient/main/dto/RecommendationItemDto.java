package com.kyumall.kyumallclient.main.dto;

import com.kyumall.kyumallcommon.main.entity.RecommendationItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class RecommendationItemDto {
  private String itemName;
  private Integer price;
  private String imageUrl;

  public static RecommendationItemDto from(RecommendationItem recommendationItem) {
    return RecommendationItemDto.builder()
        .itemName(recommendationItem.getProduct().getName())
        .price(recommendationItem.getProduct().getPrice())
//        .imageUrl(recommendationItem.getProduct().getImage())
        .build();
  }
}
