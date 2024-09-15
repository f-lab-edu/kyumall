package com.kyumall.kyumallcommon.upload.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder @AllArgsConstructor @NoArgsConstructor
@Entity
public class Image extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String originalFileName;
  private String storedFileName;

  public static Image from(UploadFile uploadFile) {
    return Image.builder()
        .originalFileName(uploadFile.getOriginalFileName())
        .storedFileName(uploadFile.getStoredFileName())
        .build();
  }
}
