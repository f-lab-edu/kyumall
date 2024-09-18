package com.kyumall.kyumalladmin.main;

import com.kyumall.kyumalladmin.main.dto.CreateBannerGroupRequest;
import com.kyumall.kyumalladmin.main.dto.CreateBannerRequest;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import com.kyumall.kyumallcommon.main.repository.BannerGroupRepository;
import com.kyumall.kyumallcommon.main.repository.BannerRepository;
import com.kyumall.kyumallcommon.upload.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BannerService {
  private final BannerGroupRepository bannerGroupRepository;
  private final BannerRepository bannerRepository;
  private final ImageUploadService imageUploadService;

  public Long createBannerGroup(CreateBannerGroupRequest request) {
    return bannerGroupRepository.save(request.toEntity()).getId();
  }

  public Long createBanner(CreateBannerRequest request, String imageName) {
    BannerGroup bannerGroup = bannerGroupRepository.findById(request.getBannerGroupId())
        .orElseThrow(() -> new KyumallException(ErrorCode.BANNER_GROUP_NOT_FOUND));



    // 임시 이미지 테이블 -> 이미지 테이블 이동
    //Image image = imageUploadService.migrateTempImageToImage(request.getImageId());

    return bannerRepository.save(request.toEntity(bannerGroup, imageName)).getId();
  }
}
