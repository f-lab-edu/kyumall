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
    String username = "test01";
    String password = "12341234";
    Member member = memberFactory.createMember(username, "test@example.com", password,
        MemberType.CLIENT);

    // when
    ExtractableResponse<Response> response = requestLogin(
        new LoginRequest(username, password));

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    assertThat(response.header("Authorization")).isNotEmpty();
  }

  @Test
  @DisplayName("토큰이 있는 경우 필터에서 토큰으로 인증에 성공합니다.")
  void authenticate_by_token_success() {
    // given
    String username = "test01";
    String email ="test@example.com";
    String password = "12341234";
    memberFactory.createMember("test01", email, password, MemberType.CLIENT);
    String token = requestLoginAndGetToken(new LoginRequest(username, password));
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .header("Authorization", token)
        .when().get("/api/auth/auth-by-token")
        .then().log().all()
        .extract();
    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    assertThat((String) response.body().jsonPath().get("result.username")).isEqualTo(username);
    assertThat((String) response.body().jsonPath().get("result.email")).isEqualTo(email);
  }

  @Test
  @DisplayName("토큰이 없는 경우 요청이 성공하고, null을 반환합니다.")
  void authenticate_by_token_token_empty_case_success() {
    // given
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .when().get("/api/auth/auth-by-token")
        .then().log().all()
        .extract();
    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
  }

  @Test
  @DisplayName("Authorization 헤더의 토큰이 유효하지 않을 경우 에러를 반환합니다.")
  void authenticate_by_token_token_fail_because() {
    // given
    String username = "test01";
    String email ="test@example.com";
    String password = "12341234";
    memberFactory.createMember("test01", email, password, MemberType.CLIENT);
    String token = requestLoginAndGetToken(new LoginRequest(username, password));
    String invalidToken = "aefawef";
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .header("Authorization", invalidToken)
        .when().get("/api/auth/auth-by-token")
        .then().log().all()
        .extract();
    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
  }

  private String requestLoginAndGetToken(LoginRequest loginRequest) {
    ExtractableResponse<Response> response = requestLogin(loginRequest);
    return response.header("Authorization");
  }

  // 토큰 만료 케이스 넣기

  // 토큰이 유효하지 않은 경우 넣기
  // 토큰이 유효하지 않은 경우, 유효하지 않은 토큰입니다. 반환

  private ExtractableResponse<Response> requestLogin(LoginRequest loginRequest) {
    return RestAssured.given().log().all()
        .body(loginRequest).contentType(ContentType.JSON)
        .when().post("/api/auth/login")
        .then().log().all()
        .extract();
  }
}
