package com.kyumall.kyumallclient.main.dto;

import com.kyumall.kyumallcommon.main.entity.Banner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class BannerDto {
  private Long id;
  private String name;
  private String url;
  private String imageUrl;
  private Integer sortOrder;

  public static BannerDto from(Banner banner) {
    return BannerDto.builder()
        .id(banner.getId())
        .name(banner.getName())
        .url(banner.getUrl())
        .imageUrl(banner.getImageName())
        .sortOrder(banner.getSortOrder())
        .build();
  }
}
