package com.kyumall.kyumallcommon.upload;

import com.kyumall.kyumallcommon.response.ResponseWrapper;
import com.kyumall.kyumallcommon.upload.dto.UploadImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class ImageUploadController {
  private final ImageUploadService imageUploadService;
  @PostMapping("/image")
  public ResponseWrapper<UploadImageResponse> uploadImage(@RequestParam("image") MultipartFile multipartFile) {
    return ResponseWrapper.ok(new UploadImageResponse(imageUploadService.uploadImage(multipartFile)));
  }
}
