package com.kyumall.kyumalladmin.main.dto;

import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder @AllArgsConstructor
@Getter
public class CreateBannerGroupRequest {
  private String name;
  private String description;

  public BannerGroup toEntity() {
    return BannerGroup.builder()
        .name(name)
        .description(description)
        .build();
  }
}
