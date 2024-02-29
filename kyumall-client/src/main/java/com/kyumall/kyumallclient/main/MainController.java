package com.kyumall.kyumallclient.main;

import com.kyumall.kyumallclient.main.dto.GetMainResponse;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/main")
@RestController
public class MainController {
  private final BannerService bannerService;
  private final RecommendService recommendService;

  @GetMapping
  public ResponseWrapper<GetMainResponse> getMain() {
    return ResponseWrapper.ok(GetMainResponse
        .builder()
          .banners(bannerService.getBanners("main"))
          .recommendations(recommendService.getRecommendations())
        .build());
  }
}
