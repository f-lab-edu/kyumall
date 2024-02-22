package com.kyumall.kyumallcommon.upload.repository;

import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface StoreImage {

  /**
   * 파일 하나를 업로드합니다.
   * @param multipartFile
   * @return
   */
  UploadFile storeImage(MultipartFile multipartFile);

  /**
   * 저장할 파일 이름을 생성합니다.
   * Unique 한 이름을 생성하여 반환합니다.
   * UUID 에 기존 파일의 확장자를 붙혀서 만듭니다.
   * @param originalFilename
   * @return
   */
  default String createStoreFileName(String originalFilename) {
    String ext = extractExt(originalFilename);
    String uuid = UUID.randomUUID().toString();
    return uuid + "." + ext;
  }

  /**
   * 파일의 확장자를 반환합니다.
   * @param originalFilename
   * @return
   */
  default String extractExt(String originalFilename) {
    int pos = originalFilename.lastIndexOf(".");
    return originalFilename.substring(pos + 1);
  }
}
