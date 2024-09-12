package com.kyumall.kyumallcommon.upload.dto;

import lombok.Getter;

@Getter
public class UploadFile {
  private String originalFileName;  // 기존의 파일 이름
  private String storedFileName;   // 업로드된 파일의 이름

  public UploadFile(String originalFileName, String storedFileName) {
    this.originalFileName = originalFileName;
    this.storedFileName = storedFileName;
  }

  public static UploadFile of(String originalFileName, String storedFileName) {
    return new UploadFile(originalFileName, storedFileName);
  }
}
