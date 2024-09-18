package com.kyumall.kyumalladmin.product;

import com.kyumall.kyumallcommon.auth.argumentResolver.LoginUser;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.dto.CreatedIdDto;
import com.kyumall.kyumallcommon.product.product.ProductService;
import com.kyumall.kyumallcommon.product.product.StockService;
import com.kyumall.kyumallcommon.product.product.dto.ProductForm;
import com.kyumall.kyumallcommon.product.product.dto.UpdateProductImageInfo;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/products")
@RequiredArgsConstructor
@RestController
public class ProductController {
  private final ProductService productService;
  private final StockService stockService;

  /**
   * 신규 상품 추가
   * @param productForm 상품 정보
   * @param images 상품 이미지
   * @param loginUser
   * @return
   */
  @PostMapping
  public ResponseWrapper<CreatedIdDto> createProduct(
      @RequestPart("productForm") @Valid ProductForm productForm,
      @RequestPart("images") @Nullable List<MultipartFile> images,
      @LoginUser AuthenticatedUser loginUser) {
    Long productId = productService.createProduct(productForm, images, loginUser.getMemberId());
    return ResponseWrapper.ok(new CreatedIdDto(productId));
  }

  /**
   * 상품 정보를 수정합니다.
   * @param id 상품 ID
   * @param productForm 상품 정보
   * @param imageInfos 이미지 정보 리스트 (기존이미지ID와 신규이미지의 파일명을 가지는 객체)
   * @param newImages  신규 이미지 multipart 형식의 입력값
   * @param loginUser
   */
  @PutMapping("/{id}")
  public void updateProduct(@PathVariable Long id,
      @RequestPart("productForm") @Valid ProductForm productForm,
      @RequestPart(value = "imageInfo", required = false) List<UpdateProductImageInfo> imageInfos,
      @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
      @LoginUser AuthenticatedUser loginUser) {
    // null 일 경우, 빈 리스트로 초기화
    imageInfos = (imageInfos == null) ? Collections.emptyList() : imageInfos;
    newImages = (newImages == null) ? Collections.emptyList() : newImages;
    productService.updateProduct(id, productForm, imageInfos, newImages, loginUser.getMemberId());
  }

  /**
   * id에 해당하는 상품의 재고 변경
   * @param id
   * @param authenticatedUser
   * @param quantity
   * @return
   */
  @PutMapping("/{id}/change-stock")
  public ResponseWrapper<Void> changeInventory(@PathVariable Long id,
      @LoginUser AuthenticatedUser authenticatedUser,
      @RequestParam Long quantity) {
    stockService.updateStock(id, authenticatedUser.getMemberId(), quantity);
    return ResponseWrapper.ok();
  }
}
