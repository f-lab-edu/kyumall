package com.kyumall.kyumalladmin.main;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumalladmin.main.dto.CreateBannerGroupRequest;
import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import com.kyumall.kyumallcommon.main.repository.BannerGroupRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("어드민 배너 통합테스트")
class BannerIntegrationTest extends IntegrationTest {

  @Autowired
  private BannerGroupRepository bannerGroupRepository;

  String[] comparedBannerGroupFieldNames = new String[] {"name", "description"};

  @Test
  @DisplayName("배너 그룹을 등록합니다.")
  void createBannerGroup_success() {
    CreateBannerGroupRequest request = CreateBannerGroupRequest.builder()
        .name("메인페이지 배너 그룹")
        .description("메인 페이지에 표시될 배너 그룹입니다.")
        .build();

    ExtractableResponse<Response> response = requestCreateBannerGroup(request);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    BannerGroup bannerGroup = bannerGroupRepository.findById(
            response.body().jsonPath().getLong("result.id")).orElseThrow(() -> new RuntimeException(""));
    assertThat(bannerGroup).usingRecursiveComparison()
        .comparingOnlyFields(comparedBannerGroupFieldNames)
        .isEqualTo(request);
  }

  private static ExtractableResponse<Response> requestCreateBannerGroup(
      CreateBannerGroupRequest request) {
    return RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/banner-group")
        .then().log().all()
        .extract();
  }
}
