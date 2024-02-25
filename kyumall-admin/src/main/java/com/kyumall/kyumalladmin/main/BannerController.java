package com.kyumall.kyumalladmin.main;

import com.kyumall.kyumalladmin.main.dto.CreateBannerGroupRequest;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping
@RestController
public class BannerController {
  private final BannerService bannerService;

  @PostMapping("/banner-groups")
  public ResponseWrapper<CreatedIdDto> createBannerGroup(@Valid @RequestBody CreateBannerGroupRequest request) {
    return ResponseWrapper.ok(CreatedIdDto.of(bannerService.createBannerGroup(request)));
  }
}
