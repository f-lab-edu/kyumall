package com.kyumall.kyumalladmin.member;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumalladmin.member.dto.SaveTermRequest;
import com.kyumall.kyumalladmin.member.dto.TermDto;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("약관 통합테스트")
class TermIntegrationTest extends IntegrationTest {
  @Autowired
  private TermRepository termRepository;

  String[] comparedTermDtoFieldNames = new String[] {"name", "ordering", "type", "status"};

  @Test
  @DisplayName("약관 생성에 성공합니다.")
  void createTerm_success() {
    // given
    SaveTermRequest request = SaveTermRequest.builder()
        .name("이용동의")
        .ordering(1)
        .type(TermType.REQUIRED)
        .status(TermStatus.INUSE)
        .build();

    // when
    ExtractableResponse<Response> response = requestCreateTerm(request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Term term = termRepository.findById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException());
    assertThat(term).usingRecursiveComparison()
        .comparingOnlyFields(comparedTermDtoFieldNames)
        .isEqualTo(request);
  }

  private static ExtractableResponse<Response> requestCreateTerm(SaveTermRequest request) {
    return RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/terms")
        .then().log().all()
        .extract();
  }

  private Term createTermForTest(SaveTermRequest request) {
    ExtractableResponse<Response> response = requestCreateTerm(request);
    return termRepository.findById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException());
  }

  @Test
  @DisplayName("id에 해당하는 약관 내용을 수정합니다.")
  void updateTerm_success() {
    // given
    SaveTermRequest insertRequest = new SaveTermRequest("이용동의 수정전", TermType.REQUIRED, TermStatus.INUSE,
        1);
    Long termId = createTermForTest(insertRequest).getId();

    SaveTermRequest updateRequest = new SaveTermRequest("이용동의 수정후", TermType.OPTIONAL, TermStatus.UNUSED,
        2);

    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .pathParam("id", termId)
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when().put("/terms/{id}")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Term term = termRepository.findById(termId).orElseThrow(() -> new RuntimeException());
    assertThat(term).usingRecursiveComparison()
        .comparingOnlyFields(comparedTermDtoFieldNames)
        .isEqualTo(updateRequest);
  }

  @Test
  @DisplayName("빈 검색어로 전체 약관을 검색합니다.")
  void searchTerm_success() {
    Term term1 = createTermForTest(
        new SaveTermRequest("이용동의약관", TermType.REQUIRED, TermStatus.INUSE, 1));
    Term term2 = createTermForTest(
        new SaveTermRequest("개인정보약관", TermType.REQUIRED, TermStatus.INUSE, 2));

    ExtractableResponse<Response> response = requestSearchTerm("");

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<TermDto> result = response.body().jsonPath().getList("result", TermDto.class);
    assertThat(result.get(0)).usingRecursiveComparison().comparingOnlyFields(
        comparedTermDtoFieldNames).isEqualTo(term1);
    assertThat(result.get(1)).usingRecursiveComparison().comparingOnlyFields(
        comparedTermDtoFieldNames).isEqualTo(term2);
  }

  @Test
  @DisplayName("검색어로 약관을 검색합니다.")
  void searchTerm_byTermName_success() {
    Term term1 = createTermForTest(
        new SaveTermRequest("이용동의약관", TermType.REQUIRED, TermStatus.INUSE, 1));
    Term term2 = createTermForTest(
        new SaveTermRequest("개인정보약관", TermType.REQUIRED, TermStatus.INUSE, 2));

    ExtractableResponse<Response> response = requestSearchTerm("이용");

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<TermDto> result = response.body().jsonPath().getList("result", TermDto.class);
    assertThat(result.get(0)).usingRecursiveComparison().comparingOnlyFields(
        comparedTermDtoFieldNames).isEqualTo(term1);
  }

  private static ExtractableResponse<Response> requestSearchTerm(String termName) {
    return RestAssured.given().log().all()
        .queryParam("termName", termName)
        .when().get("/terms")
        .then().log().all()
        .extract();
  }
}
