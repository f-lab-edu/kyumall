package com.kyumall.kyumallcommon.upload.entity;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import org.junit.jupiter.api.Test;

class ImageTest {

  @Test
  void byte_를_KB로_변환하여_이미지객체로_반환() {
    String originalFileName = "original";
    String storedFileName = "stored";
    long byteSize = 1024 * 10;
    UploadFile uploadFile = UploadFile.builder()
        .originalFileName(originalFileName)
        .storedFileName(storedFileName)
        .size(byteSize)
        .build();

    Image image = Image.from(uploadFile);

    assertThat(image.getSize()).isEqualTo(10);
  }
}
