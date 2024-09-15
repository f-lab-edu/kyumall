package com.kyumall.kyumallcommon.upload.repository;

import com.fasterxml.uuid.Generators;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class AbstractStoreImage implements StoreImage {
  private static final DateTimeFormatter simpleDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

  /**
   * 저장할 파일 이름을 생성합니다.
   *
   * 1. Unique 한 이름을 생성하여 반환합니다.
   * 2. UUID 에 기존 파일의 확장자를 붙혀서 만듭니다.
   * 3. 생성한 날짜로 폴더를 구분합니다.
   * @param originalFilename 기존 이미지명
   * @return
   */
  public String createStoreFileName(String originalFilename) {
    // 날짜
    LocalDateTime now = LocalDateTime.now();
    String todayDate = now.format(simpleDateFormatter);
    // 확장자
    String ext = extractExt(originalFilename);
    String uuidV7 = Generators.timeBasedEpochGenerator().generate().toString();
    // 조합
    return todayDate + "/" + uuidV7 + "." + ext;
  }

  /**
   * 파일의 확장자를 반환합니다.
   * @param originalFilename 기존 이미지명
   * @return
   */
  public String extractExt(String originalFilename) {
    int pos = originalFilename.lastIndexOf(".");
    return originalFilename.substring(pos + 1);
  }
}
