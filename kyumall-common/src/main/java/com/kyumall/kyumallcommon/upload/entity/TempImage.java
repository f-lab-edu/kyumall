package com.kyumall.kyumallcommon.upload.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이미지를 임시로 저장해 두는 테이블입니다.
 *
 * 공통 이미지 업로드 기능을 사용해서 이미지 업로드 시, 우선적으로 해당 테이블에 데이터가 들어가게 됩니다.
 * 이후 타 도메인에서 이미지를 확실히 저장할 경우, 데이터가 임시이미지 테이블에서 이미지 테이블 {@link Image}로 이동됩니다.
 * 임시 테이블에 존재하는 데이터들은 타 도메인에서 참조하지 않는 고아 객체들로 간주되고,
 * 배치를 이용하여 원격 저장소에 저장된 임시 테이블의 이미지를 주기적으로 삭제합니다.
 */
@Getter
@Builder @AllArgsConstructor @NoArgsConstructor
@Entity
public class TempImage extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String originalFileName;
  private String storedFileName;

  public Image convertToImageEntity() {
    return Image.builder()
        .originalFileName(originalFileName)
        .storedFileName(storedFileName)
        .build();
  }
}
