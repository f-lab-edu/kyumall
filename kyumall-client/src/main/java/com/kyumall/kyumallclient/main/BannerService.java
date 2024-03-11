package com.kyumall.kyumallclient.main;

import com.kyumall.kyumallclient.main.dto.BannerDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.main.entity.Banner;
import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import com.kyumall.kyumallcommon.main.repository.BannerGroupRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BannerService {
  private final BannerGroupRepository bannerGroupRepository;

  public List<BannerDto> getBanners(String bannerGroupName) {
    BannerGroup bannerGroup = bannerGroupRepository.findByName(bannerGroupName)
        .orElseThrow(() -> new KyumallException(ErrorCode.BANNER_GROUP_NOT_FOUND));

    return bannerGroup.getBanners().stream()
        .sorted(Comparator.comparing(Banner::getSortOrder))
        .map(BannerDto::from).toList();
  }
}
