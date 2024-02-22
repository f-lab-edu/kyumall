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

@Slf4j
@RequiredArgsConstructor
@Component
public class StoreImageToObjectStorage implements StoreImage{

  @Value("${cloud.naver.object-storage.bucket.name}")
  private String bucketName;
  private final AmazonS3 s3Client;

  @Override
  public UploadFile storeImage(MultipartFile multipartFile) {
    String storeFileName = createStoreFileName(multipartFile.getOriginalFilename());
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(multipartFile.getContentType());

    try (InputStream inputStream = multipartFile.getInputStream()) {
      s3Client.putObject(new PutObjectRequest(bucketName, storeFileName, inputStream, objectMetadata));
    } catch (IOException e) {
      throw new KyumallException(ErrorCode.FAIL_TO_IMAGE_UPLOAD);
    }

    return UploadFile.of(multipartFile.getOriginalFilename(), storeFileName);
  }
}
