package com.kyumall.kyumallcommon.upload;

import com.kyumall.kyumallcommon.Util.EncryptUtil;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import com.kyumall.kyumallcommon.upload.dto.UploadImageResponse;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class ImageUploadController {
  @Value("${encrypt.key}")
  private String encryptKey;
  private final ImageUploadService imageUploadService;
  @PostMapping("/image")
  public ResponseWrapper<UploadImageResponse> uploadImage(@RequestParam("image") MultipartFile multipartFile) {
    String storedImageName = imageUploadService.uploadImage(multipartFile);

    SecretKey secretKey = EncryptUtil.decodeStringToKey(encryptKey, EncryptUtil.ENCRYPT_ALGORITHM);
    try {
      String encryptImageName = EncryptUtil.encrypt(EncryptUtil.ENCRYPT_ALGORITHM, storedImageName,
          secretKey);
      return ResponseWrapper.ok(new UploadImageResponse(encryptImageName));
    } catch (Exception e) {
      throw new KyumallException(ErrorCode.FAIL_TO_ENCRYPT);
    }
  }
}
