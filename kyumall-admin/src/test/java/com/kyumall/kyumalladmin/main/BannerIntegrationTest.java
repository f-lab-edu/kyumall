package com.kyumall.kyumalladmin.main;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumalladmin.AuthTestUtil;
import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumalladmin.main.dto.CreateBannerGroupRequest;
import com.kyumall.kyumalladmin.main.dto.CreateBannerRequest;
import com.kyumall.kyumallcommon.Util.EncryptUtil;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.main.entity.Banner;
import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import com.kyumall.kyumallcommon.main.repository.BannerGroupRepository;
import com.kyumall.kyumallcommon.main.repository.BannerRepository;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import javax.crypto.SecretKey;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@DisplayName("배너 admin 통합테스트")
class BannerIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private BannerGroupRepository bannerGroupRepository;
  @Autowired
  private BannerRepository bannerRepository;
  @Autowired
  private ImageRepository imageRepository;
  @Value("${encrypt.key}")
  private String encryptKey;

  Member adminMike;
  String[] comparedBannerGroupFieldNames = new String[] {"name", "description"};
  String[] comparedBannerFieldNames = new String[] {"name", "description"};

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    adminMike = memberFactory.createMember(MemberFixture.MIKE);
  }

  @Test
  @DisplayName("배너 그룹을 등록합니다.")
  void createBannerGroup_success() {
    CreateBannerGroupRequest request = CreateBannerGroupRequest.builder()
        .name("메인페이지 배너 그룹")
        .description("메인 페이지에 표시될 배너 그룹입니다.")
        .build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    ExtractableResponse<Response> response = requestCreateBannerGroup(request, spec);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    BannerGroup bannerGroup = bannerGroupRepository.findById(
            response.body().jsonPath().getLong("result.id")).orElseThrow(() -> new RuntimeException(""));
    assertThat(bannerGroup).usingRecursiveComparison()
        .comparingOnlyFields(comparedBannerGroupFieldNames)
        .isEqualTo(request);
  }

  @Test
  @DisplayName("배너 등록에 성공합니다.")
  void createBanner_success() {
    // given
    BannerGroup bannerGroup = makeBannerGroup("테스트 배너 그룹");
    String originalFileName = "test.png";
    String storedFileName = "test-test-test";
    //TempImage tempImage = storeTempImage(originalFileName, storedFileName);
    SecretKey secretKey = EncryptUtil.decodeStringToKey(encryptKey, EncryptUtil.ENCRYPT_ALGORITHM);
    String encryptedFileName = "";
    try {
      encryptedFileName = EncryptUtil.encrypt(EncryptUtil.ENCRYPT_ALGORITHM, storedFileName,
          secretKey);
    }catch (Exception e) {
      throw new RuntimeException("");
    }

    CreateBannerRequest request = CreateBannerRequest.builder()
        .bannerGroupId(bannerGroup.getId())
        .name("배너1")
        .url("/test")
        .encryptedImageName(encryptedFileName).build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    // when
    ExtractableResponse<Response> response = requestCreateBanner(request, spec);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    // 이미지 엔티티
//    Image image = imageRepository.findByStoredFileName(storedFileName)
//        .orElseThrow(() -> new RuntimeException(""));
//    // 임시 이미지 엔티티에서 삭제되었는지 체크
//    assertThat(tempImageRepository.existsById(tempImage.getId())).isFalse();

    // 배너
    Banner storedBanner = bannerRepository.findById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException(""));
    assertThat(storedBanner.getBannerGroup().getId()).isEqualTo(request.getBannerGroupId());
    assertThat(storedBanner.getName()).isEqualTo(request.getName());
    assertThat(storedBanner.getUrl()).isEqualTo(request.getUrl());
    //assertThat(storedBanner.getImage().getId()).isEqualTo(image.getId());
    assertThat(storedBanner.getImageName()).isEqualTo(storedFileName);
  }

//  private TempImage storeTempImage(String originalImageName, String storedImageName) {
//    return tempImageRepository.save(TempImage.builder()
//        .originalFileName(originalImageName)
//        .storedFileName(storedImageName)
//        .build());
//  }

  private BannerGroup makeBannerGroup(String name) {
    CreateBannerGroupRequest request = CreateBannerGroupRequest.builder()
        .name(name)
        .description("test").build();
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    ExtractableResponse<Response> response = requestCreateBannerGroup(request, spec);
    return bannerGroupRepository.findById(
        response.body().jsonPath().getLong("result.id")).orElseThrow(() -> new RuntimeException(""));
  }

  private static ExtractableResponse<Response> requestCreateBannerGroup(
      CreateBannerGroupRequest request, RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/banner-groups")
        .then().log().all()
        .extract();
  }

  private static ExtractableResponse<Response> requestCreateBanner(
      CreateBannerRequest request, RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/banners")
        .then().log().all()
        .extract();
  }
}
