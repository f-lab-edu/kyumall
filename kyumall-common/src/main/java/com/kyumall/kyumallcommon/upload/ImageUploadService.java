package com.kyumall.kyumallcommon.upload;

import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import com.kyumall.kyumallcommon.upload.repository.StoreImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageUploadService {
  private final StoreImage storeImage;

  /**
   * 이미지를 스토리지에 업로드 합니다.
   * @param multipartFile
   * @return 업로드 이미지 이름
   */
  public String uploadImage(MultipartFile multipartFile) {
    UploadFile uploadFile = storeImage.store(multipartFile);
    return uploadFile.getStoredFileName();
  }
}
