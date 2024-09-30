package com.kyumall.kyumalladmin.member;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumalladmin.AuthTestUtil;
import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumalladmin.member.dto.SaveTermDetailRequest;
import com.kyumall.kyumalladmin.member.dto.SaveTermRequest;
import com.kyumall.kyumalladmin.member.dto.TermDetailDto;
import com.kyumall.kyumalladmin.member.dto.TermDto;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import com.kyumall.kyumallcommon.member.repository.TermDetailRepository;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("약관 Admin 통합테스트")
class TermIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private TermRepository termRepository;
  @Autowired
  private TermDetailRepository termDetailRepository;

  Member adminMike;
  String[] comparedTermDtoFieldNames = new String[] {"name", "ordering", "type", "status"};
  String[] comparedTermDetailDtoFieldNames = new String[] {"title", "version"};

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    adminMike = memberFactory.createMember(MemberFixture.MIKE);
  }

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
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    // when
    ExtractableResponse<Response> response = requestCreateTerm(request, spec);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Term term = termRepository.findById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException());
    assertThat(term).usingRecursiveComparison()
        .comparingOnlyFields(comparedTermDtoFieldNames)
        .isEqualTo(request);
  }

  private static ExtractableResponse<Response> requestCreateTerm(SaveTermRequest request,
      RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/terms")
        .then().log().all()
        .extract();
  }

  private Term createTermForTest(SaveTermRequest request, RequestSpecification spec) {
    ExtractableResponse<Response> response = requestCreateTerm(request, spec);
    return termRepository.findById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException());
  }

  @Test
  @DisplayName("id에 해당하는 약관 내용을 수정합니다.")
  void updateTerm_success() {
    // given
    SaveTermRequest insertRequest = new SaveTermRequest("이용동의 수정전", TermType.REQUIRED, TermStatus.INUSE,
        1);
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    Long termId = createTermForTest(insertRequest, spec).getId();

    SaveTermRequest updateRequest = new SaveTermRequest("이용동의 수정후", TermType.OPTIONAL, TermStatus.UNUSED,
        2);

    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .spec(spec)
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
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    Term term1 = createTermForTest(
        new SaveTermRequest("이용동의약관", TermType.REQUIRED, TermStatus.INUSE, 1), spec);
    Term term2 = createTermForTest(
        new SaveTermRequest("개인정보약관", TermType.REQUIRED, TermStatus.INUSE, 2), spec);

    ExtractableResponse<Response> response = requestSearchTerm("", spec);

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
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    Term term1 = createTermForTest(
        new SaveTermRequest("이용동의약관", TermType.REQUIRED, TermStatus.INUSE, 1), spec);
    Term term2 = createTermForTest(
        new SaveTermRequest("개인정보약관", TermType.REQUIRED, TermStatus.INUSE, 2), spec);

    ExtractableResponse<Response> response = requestSearchTerm("이용", spec);

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<TermDto> result = response.body().jsonPath().getList("result", TermDto.class);
    assertThat(result.get(0)).usingRecursiveComparison().comparingOnlyFields(
        comparedTermDtoFieldNames).isEqualTo(term1);
  }

  @Test
  @DisplayName("약관상세 생성에 성공합니다.")
  void createTermDetail_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    Term term = createTermForTest(new SaveTermRequest("이용동의약관", TermType.REQUIRED, TermStatus.INUSE,
        1), spec);
    SaveTermDetailRequest request = SaveTermDetailRequest.builder()
        .termId(term.getId())
        .title("이용동의약관 (필수)")
        .content("이용에 동의합니다.")
        .version(1)
        .build();

    // when
    ExtractableResponse<Response> response = requestCreateTermDetail(term.getId(), request, spec);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    TermDetail termDetail = termDetailRepository.findById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException());
    assertThat(termDetail).usingRecursiveComparison()
        .comparingOnlyFields(comparedTermDetailDtoFieldNames)
        .isEqualTo(request);
  }

  @Test
  @DisplayName("id에 해당하는 약관 상세를 수정합니다.")
  void updateTermDetail_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    Term term = createTermForTest(new SaveTermRequest("이용동의약관", TermType.REQUIRED, TermStatus.INUSE,
        1), spec);
    SaveTermDetailRequest request = SaveTermDetailRequest.builder()
        .termId(term.getId())
        .title("이용동의약관 수정후(필수)")
        .content("이용에 동의합니다.")
        .version(1)
        .build();
    TermDetail termDetail = createTermDetailForTest(request.getTermId(), request, spec);

    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .spec(spec)
        .pathParam("termId", term.getId())
        .pathParam("termDetailId", termDetail.getId())
        .contentType(ContentType.JSON)
        .body(request)
        .when().put("/terms/{termId}/details/{termDetailId}")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    TermDetail termDetail1 = termDetailRepository.findById(termDetail.getId()).orElseThrow(() -> new RuntimeException());
    assertThat(termDetail1).usingRecursiveComparison()
        .comparingOnlyFields(comparedTermDetailDtoFieldNames)
        .isEqualTo(request);
  }

  @Test
  @DisplayName("약관 ID에 해당하는 약관상세를 조회합니다.")
  void getTermDetailsByTerm_success() {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);
    Term term = createTermForTest(
        new SaveTermRequest("이용동의약관", TermType.REQUIRED, TermStatus.INUSE, 1), spec);
    SaveTermDetailRequest request1 = new SaveTermDetailRequest(term.getId(), "이용동의약관 수정후(필수)", "이용", 1);
    SaveTermDetailRequest request2 = new SaveTermDetailRequest(term.getId(), "이용동의약관 수정후(필수)", "이용", 1);
    TermDetail termDetail1 = createTermDetailForTest(term.getId(), request1, spec);
    TermDetail termDetail2 = createTermDetailForTest(term.getId(), request2, spec);

    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .spec(spec)
        .pathParam("termId", term.getId())
        .contentType(ContentType.JSON)
        .when().get("/terms/{termId}/details")
        .then().log().all()
        .extract();

    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<TermDetailDto> result = response.body().jsonPath().getList("result", TermDetailDto.class);
    assertThat(result.get(0)).usingRecursiveComparison().comparingOnlyFields(
        comparedTermDtoFieldNames).isEqualTo(termDetail1);
    assertThat(result.get(0)).usingRecursiveComparison().comparingOnlyFields(
        comparedTermDtoFieldNames).isEqualTo(termDetail2);
  }

  private TermDetail createTermDetailForTest(Long termId, SaveTermDetailRequest request,
      RequestSpecification spec) {
    ExtractableResponse<Response> response = requestCreateTermDetail(termId, request, spec);
    return termDetailRepository.findById(response.body().jsonPath().getLong("result.id"))
        .orElseThrow(() -> new RuntimeException());
  }

  private static ExtractableResponse<Response> requestCreateTermDetail(Long TermId, SaveTermDetailRequest request,
      RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .pathParam("termId", TermId)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/terms/{termId}/details")
        .then().log().all()
        .extract();
  }


  private static ExtractableResponse<Response> requestSearchTerm(String termName,
      RequestSpecification spec) {
    return RestAssured.given().log().all()
        .spec(spec)
        .queryParam("termName", termName)
        .when().get("/terms")
        .then().log().all()
        .extract();
  }
}
