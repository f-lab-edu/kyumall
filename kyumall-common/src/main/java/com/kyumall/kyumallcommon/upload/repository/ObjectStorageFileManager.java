package com.kyumall.kyumallcommon.upload.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 네이버 Cloud ObjectStorage 파일관리자
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ObjectStorageFileManager extends AbstractFileManager {

  @Value("${cloud.naver.object-storage.bucket.name}")
  private String bucketName;
  private final AmazonS3 s3Client;

  /**
   * naver cloud 에 파일을 업로드 합니다.
   * @param multipartFile
   * @return
   */
  @Override
  public UploadFile upload(MultipartFile multipartFile) {
    String storeFileName = createStoreFileName(multipartFile.getOriginalFilename());

    storeFileWithFileName(multipartFile, storeFileName);

    return UploadFile.builder()
        .storedFileName(storeFileName)
        .originalFileName(multipartFile.getOriginalFilename())
        .size(multipartFile.getSize())
        .build();
  }

  /**
   * 주어진 파일이름으로 이미지를 naver cloud objectStorage 에 업로드합니다.
   * @param multipartFile
   * @param storeFileName
   */
  @Override
  public void storeFileWithFileName(MultipartFile multipartFile, String storeFileName) {
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(multipartFile.getContentType());

    try (InputStream inputStream = multipartFile.getInputStream()) {
      s3Client.putObject(new PutObjectRequest(bucketName, storeFileName, inputStream,
          objectMetadata));
    } catch (IOException e) {
      throw new KyumallException(ErrorCode.FAIL_TO_IMAGE_UPLOAD);
    }
  }
}
