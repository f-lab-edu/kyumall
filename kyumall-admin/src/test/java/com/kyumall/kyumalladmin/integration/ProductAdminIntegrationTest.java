package com.kyumall.kyumalladmin.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyumall.kyumalladmin.AuthTestUtil;
import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.product.dto.ProductForm;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.upload.dto.UploadFile;
import com.kyumall.kyumallcommon.upload.repository.StoreImage;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ProductAdminIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductFactory productFactory;
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @MockBean
  private StoreImage storeImage;


  Member adminMike;
  Member adminBilly;

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    adminMike = memberFactory.createMember(MemberFixture.MIKE);
    adminBilly = memberFactory.createMember(MemberFixture.BILLY);
  }

  @Test
  @DisplayName("상품 생성에 성공합니다.")
  void createProduct_success(){
    // given
    Category fruit = productFactory.createCategory(CategoryFixture.FRUIT);
    ProductForm productForm = ProductForm.builder()
        .productName("얼음골 사과")
        .categoryId(fruit.getId())
        .price(30000)
        .detail("사과입니다.").build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    // when
    ExtractableResponse<Response> response = requestCreateProduct(spec, productForm);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Integer createdId = response.body().jsonPath().get("result.id");
    Product savedProduct = findProductById(createdId.longValue());
    assertThat(savedProduct.getName()).isEqualTo(productForm.getProductName());
    assertThat(savedProduct.getCategory().getId()).isEqualTo(productForm.getCategoryId());
    assertThat(savedProduct.getSeller().getId()).isEqualTo(adminMike.getId());
    assertThat(savedProduct.getPrice()).isEqualTo(productForm.getPrice());
    assertThat(savedProduct.getDetail()).isEqualTo(productForm.getDetail());
  }

  @Test
  @DisplayName("상품 생성에 성공합니다.(상품 이미지 등록에 성공)")
  void createProduct_with_productImage_success() {
    // given
    Category fruit = productFactory.createCategory(CategoryFixture.FRUIT);
    ProductForm request = ProductForm.builder()
        .productName("얼음골 사과")
        .categoryId(fruit.getId())
        .price(30000)
        .detail("사과입니다.").build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    // 이미지 업로드 모킹
    byte[] fakeFileBytes = "fakeImage".getBytes();
    String storedFileName = "STORED_FILE_NAME";
    String originalFileName = "test-image1.jpeg";
    Long size = 100000L;
    given(storeImage.store(any())).willReturn(new UploadFile(originalFileName, storedFileName, size));

    // when
    ExtractableResponse<Response> response = requestCreateProduct(spec, request, new FakeImage(originalFileName, fakeFileBytes));

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Integer createdId = response.body().jsonPath().get("result.id");
    Product savedProduct = findProductById(createdId.longValue());
    assertThat(savedProduct.getName()).isEqualTo(request.getProductName());
    assertThat(savedProduct.getCategory().getId()).isEqualTo(request.getCategoryId());
    assertThat(savedProduct.getSeller().getId()).isEqualTo(adminMike.getId());
    assertThat(savedProduct.getPrice()).isEqualTo(request.getPrice());
    assertThat(savedProduct.getDetail()).isEqualTo(request.getDetail());
    Product productWithImages = productRepository.findWithImagesById(createdId.longValue()).orElseThrow();
    assertThat(productWithImages.getProductImages().get(0).getImage().getId()).isEqualTo(storedFileName);
  }

  @Test
  @DisplayName("상품의 정보 변경에 성공합니다.")
  void updateProduct_success() {
    Category category1 = categoryRepository.saveAndFlush(CategoryFixture.FOOD.toEntity());
    Category category2 = categoryRepository.saveAndFlush(CategoryFixture.FASHION.toEntity());
    Product product = productRepository.saveAndFlush(ProductFixture.APPLE.toEntity(adminMike, category1));
    ProductForm productForm = ProductForm.builder()
        .productName("updated name")
        .price(7777)
        .detail("updated details")
        .categoryId(category2.getId())
        .build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    // when
    ExtractableResponse<Response> response = requestUpdateProduct(spec, product.getId(), productForm);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Product productSaved = productRepository.findById(product.getId()).orElseThrow();
    assertThat(productSaved.getName()).isEqualTo(productForm.getProductName());
    assertThat(productSaved.getPrice()).isEqualTo(productForm.getPrice());
    assertThat(productSaved.getDetail()).isEqualTo(productForm.getDetail());
    assertThat(productSaved.getCategory().getId()).isEqualTo(productForm.getCategoryId());
  }

  @Test
  @DisplayName("상품을 등록한 관리자가 아니라서 상품 변경에 실패합니다.")
  void updateProduct_fail_because_of_not_register() {
    Category category1 = categoryRepository.saveAndFlush(CategoryFixture.FOOD.toEntity());
    Category category2 = categoryRepository.saveAndFlush(CategoryFixture.FASHION.toEntity());
    Product product = productRepository.saveAndFlush(ProductFixture.APPLE.toEntity(adminMike, category1));
    ProductForm productForm = ProductForm.builder()
        .productName("updated name")
        .price(7777)
        .detail("updated details")
        .categoryId(category2.getId())
        .build();
    // 상품 등록자 아님
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminBilly.getUsername(), MemberFixture.password);

    // when
    ExtractableResponse<Response> response = requestUpdateProduct(spec, product.getId(), productForm);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
  }

  private ExtractableResponse<Response> requestCreateProduct(RequestSpecification spec, ProductForm productForm, FakeImage... fakeImages) {
    RequestSpecification requestSpecification = RestAssured.given().log().all()
        .spec(spec)
        .contentType(ContentType.MULTIPART)
        .multiPart(new MultiPartSpecBuilder(productForm)
            .controlName("productForm")
            .mimeType("application/json")
            .charset("UTF-8")
            .build()
        );
    // 이미지 있는 경우 반복문 돌면서 추가
    for (FakeImage fakeImage: fakeImages) {
      requestSpecification.multiPart("images", fakeImage.fileName ,fakeImage.byteArray);
    }
    ExtractableResponse<Response> response = requestSpecification.when().post("/products")
        .then().log().all()
        .extract();

    return response;
  }

  private static ExtractableResponse<Response> requestUpdateProduct(RequestSpecification spec, Long productId, ProductForm request) {
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .spec(spec)
        .body(request)
        .pathParam("id", productId)
        .when().put("/products/{id}")
        .then().log().all()
        .extract();
    return response;
  }

  private Product findProductById(Long productId) {
    return productRepository.findWithFetchById(productId)
        .orElseThrow(() -> new RuntimeException());
  }

  class FakeImage {
    public String fileName;
    public byte[] byteArray;

    public FakeImage(String fileName, byte[] byteArray) {
      this.fileName = fileName;
      this.byteArray = byteArray;
    }
  }
}
