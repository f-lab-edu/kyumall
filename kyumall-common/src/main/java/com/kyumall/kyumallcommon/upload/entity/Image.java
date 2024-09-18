package com.kyumall.kyumallcommon.upload.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = "id")
@Getter
@Builder @AllArgsConstructor @NoArgsConstructor
@Entity
public class Image extends BaseTimeEntity {
  @Id
  private String id;
  private String originalFileName;    // 원래 이미지명
  private String storedFileName;    // 저장소에 저장된 이미지명
  private Double size;    // KB 단위로 환산하여 저장합니다.

  public static Image from(UploadFile uploadFile) {
    return Image.builder()
        .id(uploadFile.getStoredFileName())   // storedFileName 과 동일
        .originalFileName(uploadFile.getOriginalFileName())
        .storedFileName(uploadFile.getStoredFileName())
        .size(convertByteToKB(uploadFile.getSize()))
        .build();
  }

  public static Image from(String id) {
    return Image.builder().id(id).build();
  }

  private static Double convertByteToKB(long byteSize) {
    double mbSize = byteSize / (1024.0);
    return Math.floor(mbSize * 1_000) / 1_000;  // 소수점 3자리 이하 버림
  }
}
