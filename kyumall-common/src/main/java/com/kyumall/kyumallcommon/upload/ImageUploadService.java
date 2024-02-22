package com.kyumall.kyumallcommon.upload;

import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import com.kyumall.kyumallcommon.upload.repository.StoreImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageUploadService {
  private final StoreImage storeImage;
  private final ImageRepository imageRepository;

  public Long uploadImage(MultipartFile multipartFile) {
    UploadFile uploadFile = storeImage.storeImage(multipartFile);
    return imageRepository.save(uploadFile.toEntity()).getId();
  }
}
