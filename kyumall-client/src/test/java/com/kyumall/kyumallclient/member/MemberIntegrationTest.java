package com.kyumall.kyumallclient.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.TestUtil;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.mail.MailService;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import com.kyumall.kyumallcommon.member.vo.VerificationStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.Clock;
import java.time.LocalDateTime;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@DisplayName("회원 통합테스트")
class MemberIntegrationTest extends IntegrationTest {
  @MockBean
  MailService mailService;
  @MockBean
  Clock clock;
  @MockBean
  RandomCodeGenerator randomCodeGenerator;
  @Autowired
  VerificationRepository verificationRepository;

  @Test
  @DisplayName("본인 확인 이메일을 보내는데 성공합니다.")
  void sendVerificationMail_success() {
    // given
    String email = "example@example.com";
    LocalDateTime sendDatetime = LocalDateTime.of(2024,1,1,0,0);
    String code = "000000";

    // when
    ExtractableResponse<Response> response = requestSendVerificationMail(email, sendDatetime, code);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    // 메일발송 메서드 호출 체크
    then(mailService).should(times(1)).sendMail(email);
    // DB 저장 체크
    Verification verification = verificationRepository.findUnverifiedByEmail(email)
        .orElseThrow(() -> new RuntimeException("테스트 실패"));
    assertThat(verification.getContact()).isEqualTo(email);
    assertThat(verification.getCode()).isEqualTo(code);
    assertThat(verification.getSendDateTime()).isEqualTo(sendDatetime);
    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.UNVERIFIED);
    assertThat(verification.getTryCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("이미 전송한 이력이 있고, 3분이 지나지 않아서 재전송에 실패합니다.")
  void sendVerificationMail_fail_because_doesnt_pass_3_minute() {
    // given
    String email = "example@example.com";
    String code = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 첫 메일 전송
    requestSendVerificationMail(email, firstSendTime, code);

    // when
    LocalDateTime secondSendTime = firstSendTime.plusMinutes(1);
    ExtractableResponse<Response> response = requestSendVerificationMail(email, secondSendTime, code);

    // then
    assertThat(response.statusCode()).isNotEqualTo(HttpStatus.SC_OK); // 성공 아님
    // 메일발송 메서드 호출 체크 (첫메일 전송 한번)
    then(mailService).should(times(1)).sendMail(email);
  }

  @Test
  @DisplayName("이미 전송한 이력이 있지만, 3분이 지나서 메일을 재전송합니다.")
  void sendVerificationMail_resend_success() {
    // given
    String email = "example@example.com";
    String code = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 첫 메일 전송
    requestSendVerificationMail(email, firstSendTime, code);

    // when
    LocalDateTime secondSendTime = firstSendTime.plusMinutes(3);
    ExtractableResponse<Response> response = requestSendVerificationMail(email, secondSendTime, code);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    // 메일발송 메서드 호출 체크 (첫메일, 재전송 합쳐서 두번)
    then(mailService).should(times(2)).sendMail(email);
  }

  @Test
  @DisplayName("본인인증에 성공합니다.")
  void verifySentCode_success() {
    // given
    String email = "example@example.com";
    String code = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 본인인증 메일 전송
    requestSendVerificationMail(email, firstSendTime, code);

    // when
    ExtractableResponse<Response> response = requestVerifySentCode(email, code);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Verification verification = verificationRepository.findByContact(email)
        .orElseThrow(() -> new RuntimeException());
    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
  }

  @Test
  @DisplayName("인증번호가 맞지 않아 본인인증에 실패하고 시도횟수를 1 증가시킵니다.")
  void verifySentCode_fail_because_code_not_match() {
    // given
    String email = "example@example.com";
    String rightCode = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 본인인증 메일 전송
    requestSendVerificationMail(email, firstSendTime, rightCode);
    String wrongCode = "000011";

    // when
    ExtractableResponse<Response> response = requestVerifySentCode(email, wrongCode);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    Verification verification = verificationRepository.findByContact(email)
        .orElseThrow(() -> new RuntimeException());
    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.UNVERIFIED);
    assertThat(verification.getTryCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("인증번호가 맞지 않고, 시도횟수가 초과하여 본인인증에 실패하고, 인증번호는 만료됩니다.")
  void verifySentCode_fail_because_code_not_match_and_exceed_try() {
    // given
    String email = "example@example.com";
    String rightCode = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 본인인증 메일 전송
    requestSendVerificationMail(email, firstSendTime, rightCode);
    String wrongCode = "000011";
    requestVerifySentCode(email, wrongCode); // try 1
    requestVerifySentCode(email, wrongCode); // try 2
    requestVerifySentCode(email, wrongCode); // try 3

    // when
    ExtractableResponse<Response> response = requestVerifySentCode(email, wrongCode);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    Verification verification = verificationRepository.findByContact(email)
        .orElseThrow(() -> new RuntimeException());
    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.EXPIRED);
  }

  private ExtractableResponse<Response> requestSendVerificationMail(String email, LocalDateTime sendDateTime, String code) {
    // 전송시간 모킹
    Clock sendTimeClock = TestUtil.convertLocalDateTimeToClock(sendDateTime);
    given(clock.instant()).willReturn(sendTimeClock.instant());
    given(clock.getZone()).willReturn(sendTimeClock.getZone());
    // 랜덤 로직 모킹
    given(randomCodeGenerator.generateCode(anyInt())).willReturn(code);

    return RestAssured.given().log().all()
        .queryParam("email", email)
        .when().post("/members/send-verification-mail")
        .then().log().all()
        .extract();
  }

  private ExtractableResponse<Response> requestVerifySentCode(String email, String code) {
    VerifySentCodeRequest request = new VerifySentCodeRequest(email, code);

    return RestAssured.given().log().all()
        .body(request).contentType(ContentType.JSON)
        .when().post("/members/verify-sent-code")
        .then().log().all()
        .extract();
  }
}