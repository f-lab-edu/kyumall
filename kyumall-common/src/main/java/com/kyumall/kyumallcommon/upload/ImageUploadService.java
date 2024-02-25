package com.kyumall.kyumallcommon.upload;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import com.kyumall.kyumallcommon.upload.entity.Image;
import com.kyumall.kyumallcommon.upload.entity.TempImage;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import com.kyumall.kyumallcommon.upload.repository.StoreImage;
import com.kyumall.kyumallcommon.upload.repository.TempImageRepository;
import java.util.List;
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
  private final TempImageRepository tempImageRepository;

  /**
   * 임시 테이블에 이미지를 업로드 합니다.
   * @param multipartFile
   * @return
   */
  public Long uploadImage(MultipartFile multipartFile) {
    UploadFile uploadFile = storeImage.store(multipartFile);
    return tempImageRepository.save(uploadFile.toEntity()).getId();
  }

  /**
   * 임시 이미지 테이블에 저장된 이미지를 이미지 테이블로 이동시킵니다.
   * @param tempImageIds 임시이미지 ID 리스트
   * @return 저장된 이미지 객체 리스트
   */
  public List<Image> migrateTempImageToImage(List<Long> tempImageIds) {
    List<TempImage> tempImages = tempImageRepository.findByIdIn(tempImageIds);
    if (tempImages.size() != tempImageIds.size()) {
      throw new KyumallException(ErrorCode.TEMP_IMAGE_ID_NOT_EXISTS);
    }
    List<Image> images = imageRepository.saveAll(
        tempImages.stream().map(TempImage::convertToImageEntity).toList());
    // 임시 이미지 삭제
    tempImageRepository.deleteAllInBatch(tempImages);
    return images;
  }

  /**
   * 임시 이미지 테이블에 저장된 이미지를 이미지 테이블로 이동시킵니다.
   * @param tempImageId 임시이미지 ID
   * @return 저장된 이미지 객체
   */
  public Image migrateTempImageToImage(Long tempImageId) {
    TempImage tempImage = tempImageRepository.findById(tempImageId)
        .orElseThrow(() -> new KyumallException(ErrorCode.TEMP_IMAGE_ID_NOT_EXISTS));
    Image image = imageRepository.save(tempImage.convertToImageEntity());
    // 임시 이미지 테이블에서 삭제
    tempImageRepository.delete(tempImage);
    return image;
  }
}
