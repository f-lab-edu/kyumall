package com.kyumall.kyumallcommon.upload;

import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import com.kyumall.kyumallcommon.upload.entity.Image;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import com.kyumall.kyumallcommon.upload.repository.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageUploadService {
  private final FileManager fileManager;
  private final ImageRepository imageRepository;

  /**
   * 이미지를 스토리지에 업로드 합니다.
   * @param multipartFile
   * @return 업로드 이미지 이름
   */
  @Transactional
  public Image uploadImage(MultipartFile multipartFile) {
    UploadFile uploadFile = fileManager.upload(multipartFile);
    return imageRepository.save(Image.from(uploadFile));
  }
}
