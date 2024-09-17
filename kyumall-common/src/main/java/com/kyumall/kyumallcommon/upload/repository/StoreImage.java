package com.kyumall.kyumallcommon.upload.repository;

import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import org.springframework.web.multipart.MultipartFile;

public interface StoreImage {

  /**
   * 파일 하나를 업로드합니다.
   * 사용자가 파일을 업로드 하기 위해 로직 내에서 호출하는 고수준 메서드 입니다.
   * @param multipartFile
   * @return
   */
  UploadFile upload(MultipartFile multipartFile);

  /**
   * 입력받은 파일이름으로 파일을 저장소에 저장합니다.
   * 파일을 저장소에 업로드 하는 역할을 담당하는 저수준 메서드 입니다.
   * upload 내부에서 호출되지만, 외부 의존성이 있는 부분을 분리해여 테스트코드에서 Mocking 할 수 있도록 분리하였습니다.
   */
  void storeFileWithFileName(MultipartFile multipartFile, String storeFileName);
}
