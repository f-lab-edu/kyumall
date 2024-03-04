package com.kyumall.kyumalladmin.main;

import com.kyumall.kyumalladmin.main.dto.CreateBannerGroupRequest;
import com.kyumall.kyumalladmin.main.dto.CreateBannerRequest;
import com.kyumall.kyumallcommon.Util.EncryptUtil;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import jakarta.validation.Valid;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping
@RestController
public class BannerController {
  private final BannerService bannerService;
  @Value("${encrypt.key}")
  private String encryptKey;

  @PostMapping("/banner-groups")
  public ResponseWrapper<CreatedIdDto> createBannerGroup(@Valid @RequestBody CreateBannerGroupRequest request) {
    return ResponseWrapper.ok(CreatedIdDto.of(bannerService.createBannerGroup(request)));
  }

  @PostMapping("/banners")
  public ResponseWrapper<CreatedIdDto> createBanner(@Valid @RequestBody CreateBannerRequest request) {
    SecretKey secretKey = EncryptUtil.decodeStringToKey(encryptKey, EncryptUtil.ENCRYPT_ALGORITHM);
    try {
      String imageName = EncryptUtil.decrypt(EncryptUtil.ENCRYPT_ALGORITHM,
          request.getEncryptedImageName(),
          secretKey);
      return ResponseWrapper.ok(CreatedIdDto.of(bannerService.createBanner(request, imageName)));
    } catch (Exception e) {
      throw new KyumallException(ErrorCode.FAIL_TO_DECRYPT);
    }


  }
}
