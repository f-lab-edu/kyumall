package com.kyumall.kyumallcommon.fixture.common;

import com.kyumall.kyumallcommon.upload.entity.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageFixture {
  PIZZA_IMAGE("pizza.jpeg", "2024/10/21/EJFD-EJFD-KEKW-KDJE", 11.2),
  CHICKEN_IMAGE("chicken.jpeg", "2024/10/23/EKDJ-DWEF-QDSF-KCKD", 10.2);

  private final String originalFileName;
  private final String storedFileName;
  private final Double size;

  public Image toEntity() {
    return Image.builder()
        .id(storedFileName)
        .originalFileName(originalFileName)
        .storedFileName(storedFileName)
        .size(size)
        .build();
  }


}
