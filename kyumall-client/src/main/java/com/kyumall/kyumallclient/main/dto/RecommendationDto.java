package com.kyumall.kyumallclient.main.dto;

import com.kyumall.kyumallcommon.main.entity.Recommendation;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder @AllArgsConstructor
@Getter
public class RecommendationDto {
  private String title;
  private String displayText;
  private List<RecommendationItemDto> recommendationItemDtos;

  public static RecommendationDto from(Recommendation recommendation) {
    return RecommendationDto.builder()
        .title(recommendation.getTitle())
        .displayText(recommendation.getDisplayText())
        .recommendationItemDtos(
            recommendation.getRecommendationItems()
            .stream()
            .map(RecommendationItemDto::from).toList())
        .build();
  }
}
