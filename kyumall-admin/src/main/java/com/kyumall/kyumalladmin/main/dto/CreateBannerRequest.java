package com.kyumall.kyumalladmin.main.dto;

import com.kyumall.kyumallcommon.main.entity.Banner;
import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import com.kyumall.kyumallcommon.upload.entity.Image;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class CreateBannerRequest {
  @NotNull
  private Long bannerGroupId;
  @NotEmpty
  private String name;
  @NotEmpty
  private String url;
  @NotEmpty
  private String encryptedImageName;

  public Banner toEntity(BannerGroup bannerGroup, String imageName) {
    return Banner.builder()
        .bannerGroup(bannerGroup)
        .name(name)
        .url(url)
        .imageName(imageName)
        .build();
  }
}
