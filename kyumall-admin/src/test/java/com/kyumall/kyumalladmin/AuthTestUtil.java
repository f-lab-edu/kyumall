package com.kyumall.kyumalladmin;

import com.kyumall.kyumallcommon.auth.dto.LoginRequest;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class AuthTestUtil extends IntegrationTest {
  public static ExtractableResponse<Response> requestLogin(String username, String password) {
    return RestAssured.given().log().all()
        .body(new LoginRequest(username, password))
        .contentType(ContentType.JSON)
        .when().post("/api/auth/login")
        .then().log().all()
        .extract();
  }

  public static String requestLoginAndGetToken(String username,  String password) {
    ExtractableResponse<Response> response = requestLogin(username, password);
    return response.header("Authorization");
  }

  public static RequestSpecification requestLoginAndGetSpec(String username,  String password) {
    String token = requestLoginAndGetToken(username, password);
    return new RequestSpecBuilder().addHeader("Authorization", token).build();
  }
}
