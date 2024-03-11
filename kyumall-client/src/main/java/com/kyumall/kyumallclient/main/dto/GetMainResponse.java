package com.kyumall.kyumallclient.main.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor @Builder
@Getter
public class GetMainResponse {
  @Builder.Default
  private List<BannerDto> banners = new ArrayList<>();
  @Builder.Default
  private List<RecommendationDto> recommendations = new ArrayList<>();
}
