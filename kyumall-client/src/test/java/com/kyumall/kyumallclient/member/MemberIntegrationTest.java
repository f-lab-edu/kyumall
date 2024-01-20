package com.kyumall.kyumallclient.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.TestUtil;
import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallclient.member.dto.TermAndAgree;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.mail.MailService;
import com.kyumall.kyumallcommon.member.entity.Agreement;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.repository.AgreementRepository;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import com.kyumall.kyumallcommon.member.vo.TermType;
import com.kyumall.kyumallcommon.member.vo.VerificationStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
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
  @Autowired
  TermRepository termRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  AgreementRepository agreementRepository;

  private Term privateInfoTerm;
  private Term marketingTerm;

  @BeforeEach
  void init_data() {
    privateInfoTerm = termRepository.save(Term.builder()
        .name("개인정보 수집 및 이용 동의")
        .content("개인정 수집 및 이용에 동의합니다.")
        .type(TermType.REQUIRED).build());

    marketingTerm = termRepository.save(Term.builder()
        .name("마케팅 목적의 개인정보 수집 및 이용 동의 (선택)")
        .content("마케팅 목적으로 개인정보를 수집하고 이용하는 것에 동의합니다.")
        .type(TermType.OPTIONAL).build());
  }

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
    Verification verification = verificationRepository.findUnverifiedByContact(email)
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

  @Test
  @DisplayName("회원가입에 성공합니다.")
  void signUp_success() {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .username("username1")
        .email("email1@example.com")
        .password("password1")
        .passwordCheck("password1")
        .termAndAgrees(List.of(
            new TermAndAgree(privateInfoTerm.getId(), true),
            new TermAndAgree(marketingTerm.getId(), false)))
        .build();
    // 본인 인증 성공 처리
    sendMailAndValidComplete(request.getEmail());

    // when
    ExtractableResponse<Response> response = requestSignUp(request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    // 회원 객체 검증
    Member member = memberRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new RuntimeException());
    assertThat(member.getUsername()).isEqualTo(request.getUsername());
    assertThat(member.getEmail()).isEqualTo(request.getEmail());
    assertThat(member.getStatus()).isEqualTo(MemberStatus.INUSE);
    assertThat(member.getType()).isEqualTo(MemberType.USER);
    // 동의 객체 검증
    List<Agreement> agreements = agreementRepository.findByMember(member);
    assertThat(agreements).hasSize(2);
  }

  @Test
  @DisplayName("중복된 username 이 존재하여 회원가입에 실패합니다.")
  void signUp_fail_because_duplicate_username() {
    // given
    // 첫번째 회원가입
    SignUpRequest request1 = new SignUpRequest("username1", "email1@example.com", "password1", "password1",
        List.of(new TermAndAgree(privateInfoTerm.getId(), true),
            new TermAndAgree(marketingTerm.getId(), false)));

    sendMailAndValidComplete(request1.getEmail());
    requestSignUp(request1);

    // 중복된 아이디 회원가입
    SignUpRequest request2 = new SignUpRequest("username1", "email2@example.com", "password1", "password1",
        List.of(new TermAndAgree(privateInfoTerm.getId(), true),
            new TermAndAgree(marketingTerm.getId(), false)));
    sendMailAndValidComplete(request2.getEmail());
    // when
    ExtractableResponse<Response> response = requestSignUp(request2);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("중복된 email 이 존재하여 회원가입에 실패합니다.")
  void signUp_fail_because_duplicate_email() {
    // given
    // 첫번째 회원가입
    SignUpRequest request1 = new SignUpRequest("username1", "email1@example.com", "password1", "password1",
        List.of(new TermAndAgree(privateInfoTerm.getId(), true),
            new TermAndAgree(marketingTerm.getId(), false)));

    sendMailAndValidComplete(request1.getEmail());
    requestSignUp(request1);

    // 중복된 아이디 회원가입
    SignUpRequest request2 = new SignUpRequest("username2", "email1@example.com", "password1", "password1",
        List.of(new TermAndAgree(privateInfoTerm.getId(), true),
            new TermAndAgree(marketingTerm.getId(), false)));
    sendMailAndValidComplete(request2.getEmail());
    // when
    ExtractableResponse<Response> response = requestSignUp(request2);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않아 회원가입에 실패합니다.")
  void signUp_fail_because_not_match_passwordCheck() {
    // given
    // 첫번째 회원가입
    SignUpRequest request = new SignUpRequest("username1", "email1@example.com", "password1", "password2",
        List.of(new TermAndAgree(privateInfoTerm.getId(), true),
            new TermAndAgree(marketingTerm.getId(), false)));

    sendMailAndValidComplete(request.getEmail());

    // when
    ExtractableResponse<Response> response = requestSignUp(request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("필수 약관에 동의하지 않아 회원가입에 실패합니다.")
  void signUp_fail_because_not_agree_on_required_term() {
    // given
    // 첫번째 회원가입
    SignUpRequest request = new SignUpRequest("username1", "email1@example.com", "password1", "password1",
        List.of(new TermAndAgree(privateInfoTerm.getId(), false), // 필수약관 미동의
            new TermAndAgree(marketingTerm.getId(), false)));

    sendMailAndValidComplete(request.getEmail());

    // when
    ExtractableResponse<Response> response = requestSignUp(request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  /*
  회원가입 요청을 보냅니다.
   */
  private static ExtractableResponse<Response> requestSignUp(SignUpRequest request) {
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/members/sign-up")
        .then().log().all()
        .extract();
    return response;
  }

  /**
   * 본인인증메일을 보내고 인증 확인까지 통과합니다.
   */
  private void sendMailAndValidComplete(String email) {
    String code = "000000";
    LocalDateTime sendTime = LocalDateTime.of(2024,1,1,1,1);
    requestSendVerificationMail(email, sendTime, code);
    requestVerifySentCode(email, code);
  }

  /*
  인증메일을 전송 요청을 보냅니다.
   */
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

  /*
  본인 인증 요청을 보냅니다.
   */
  private ExtractableResponse<Response> requestVerifySentCode(String email, String code) {
    VerifySentCodeRequest request = new VerifySentCodeRequest(email, code);

    return RestAssured.given().log().all()
        .body(request).contentType(ContentType.JSON)
        .when().post("/members/verify-sent-code")
        .then().log().all()
        .extract();
  }
}
