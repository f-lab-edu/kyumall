package com.kyumall.kyumallcommon.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UploadImageResponse {
  //private Long imageId;
  private String encryptedImageName; // 암호화 한 이미지명
}
