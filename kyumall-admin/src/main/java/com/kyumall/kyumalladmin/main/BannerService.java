package com.kyumall.kyumalladmin.main;

import com.kyumall.kyumalladmin.main.dto.CreateBannerGroupRequest;
import com.kyumall.kyumallcommon.main.repository.BannerGroupRepository;
import com.kyumall.kyumallcommon.main.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BannerService {
  private final BannerGroupRepository bannerGroupRepository;
  private final BannerRepository bannerRepository;
  public Long createBannerGroup(CreateBannerGroupRequest request) {
    return bannerGroupRepository.save(request.toEntity()).getId();
  }
}
