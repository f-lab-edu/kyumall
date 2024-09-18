package com.kyumall.kyumallcommon.product.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상품정보 수정을 위한 이미지 입력값
 * 이미지 순서(sequence) 관리를 위해 기존 이미지(url형태)의 imageId와 신규 이미지(byte[] 형태)의 파일명을 담는 객체입니다.
 * List에 담겨서 List의 인덱스 별로 이미지 순서(sequence) 가 매겨집니다.
 * 기존 이미지는 url image
 */
@AllArgsConstructor
@Getter
public class UpdateProductImageInfo {
  private String imageId;   // 기존 이미지일 경우, 이미지 ID
  private String newImageFileName;  // 신규 이미지일 경우, 파일 이름
  private boolean isNew;    // 신규 이미지인지 여부
}
