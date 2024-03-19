package com.kyumall.kyumallcommon.auth;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.IntegrationTest;
import com.kyumall.kyumallcommon.MemberFactory;
import com.kyumall.kyumallcommon.auth.dto.LoginRequest;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Auth 통합테스트")
class AuthIntegrationTest extends IntegrationTest {
  @Autowired
  MemberFactory memberFactory;

  @Test
  @DisplayName("로그인에 성공합니다.")
  void login_success() {
    // given
    String password = "12341234";
    Member member = memberFactory.createMember("test01", "test@example.com", password,
        MemberType.CLIENT);

    // when
    ExtractableResponse<Response> response = requestLogin(
        new LoginRequest(member.getUsername(), password));

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.header("Authorization")).isNotEmpty();
  }

  private ExtractableResponse<Response> requestLogin(LoginRequest loginRequest) {
    return RestAssured.given().log().all()
        .body(loginRequest).contentType(ContentType.JSON)
        .when().post("/api/auth/login")
        .then().log().all()
        .extract();
  }
}
