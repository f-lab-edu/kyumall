package com.kyumall.kyumallcommon.upload.repository;

import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import org.springframework.web.multipart.MultipartFile;

public interface StoreImage {

  /**
   * 파일 하나를 업로드합니다.
   * @param multipartFile
   * @return
   */
  UploadFile store(MultipartFile multipartFile);
}
