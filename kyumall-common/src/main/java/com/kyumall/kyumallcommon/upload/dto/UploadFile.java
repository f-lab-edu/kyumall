package com.kyumall.kyumallcommon.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 업로드한 파일의 정보
 */
@AllArgsConstructor @Builder
@Getter
public class UploadFile {
  private String originalFileName;  // 기존의 파일 이름
  private String storedFileName;   // 업로드된 파일의 이름
  private Long size;
}
