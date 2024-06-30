package com.kyumall.kyumallclient.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.TestUtil;
import com.kyumall.kyumallclient.member.dto.RecoverPasswordRequest;
import com.kyumall.kyumallclient.member.dto.ResetPasswordRequest;
import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallclient.member.dto.TermDto;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallcommon.Util.EncryptUtil;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.factory.EmailFactory;
import com.kyumall.kyumallcommon.fixture.member.TermDetailFixture;
import com.kyumall.kyumallcommon.fixture.member.TermFixture;
import com.kyumall.kyumallcommon.member.entity.Agreement;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.repository.AgreementRepository;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.repository.TermDetailRepository;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import com.kyumall.kyumallcommon.member.vo.VerificationStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

@DisplayName("회원 통합테스트")
class MemberIntegrationTest extends IntegrationTest {
  @MockBean
  Clock clock;
  @MockBean
  RandomCodeGenerator randomCodeGenerator;
  @Autowired
  VerificationRepository verificationRepository;
  @Autowired
  TermRepository termRepository;
  @Autowired
  TermDetailRepository termDetailRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  AgreementRepository agreementRepository;
  @Autowired
  EmailFactory emailFactory;

  @Value("${encrypt.key}")
  private String encryptKey;
  private SecretKey secretKey;

  private Term privateInfoTerm;
  private Term marketingTerm;
  private TermDetail kyumallTermDetail;
  private TermDetail marketingTermDetail1;
  private TermDetail marketingTermDetail2;

  @BeforeEach
  void init_data() {
    secretKey = EncryptUtil.decodeStringToKey(encryptKey, MemberService.ID_ENCRYPTION_ALGORITHM);
    // 약관 데이터
    privateInfoTerm = termRepository.save(TermFixture.PRIVACY.createEntity());
    marketingTerm = termRepository.save(TermFixture.MARKETING.createEntity());
    // 약관 상세 데이터
    kyumallTermDetail = termDetailRepository.save(TermDetailFixture.PRIVACY_DETAIL.createEntity(privateInfoTerm));
    marketingTermDetail1 = termDetailRepository.save(TermDetailFixture.MARKETING_DETAIL_1.createEntity(marketingTerm));
    marketingTermDetail2 = termDetailRepository.save(TermDetailFixture.MARKETING_DETAIL_2.createEntity(marketingTerm));
    // 이메일 템플릿
    emailFactory.createEmailTemplate(MemberService.SIGNUP_VERIFICATION_EMAIL_TEMPLATE_ID, "Kyumall 계정 인증", "email-template/signup-verification-template");
    emailFactory.createEmailTemplate(MemberService.TEMPORARY_PASSWORD_EMAIL_TEMPLATE_ID, "Kyumall 계정 비밀번호 재설정", "email-template/temporary-password-template");
  }

  @Test
  @DisplayName("본인 확인 이메일을 보내는데 성공합니다.")
  void sendVerificationMail_success()
      throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    // given
    String email = "example@example.com";
    LocalDateTime sendDatetime = LocalDateTime.of(2024,1,1,0,0);
    String code = "000000";

    // when
    ExtractableResponse<Response> response = requestSendVerificationMail(email, sendDatetime, code);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    // DB 저장 체크
    Verification verification = verificationRepository.findUnverifiedByContact(email)
        .orElseThrow(() -> new RuntimeException("테스트 실패"));
    assertThat(verification.getContact()).isEqualTo(email);
    assertThat(verification.getCode()).isEqualTo(code);
    assertThat(verification.getSendDateTime()).isEqualTo(sendDatetime);
    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.UNVERIFIED);
    assertThat(verification.getTryCount()).isEqualTo(0);
    // 암호화하여 반환한 ID값 체크
    String verificationId = response.body().jsonPath().get("result.key");
    String decryptId = EncryptUtil.decrypt(MemberService.ID_ENCRYPTION_ALGORITHM, verificationId, secretKey);
    assertThat(Long.parseLong(decryptId)).isEqualTo(verification.getId());
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
//    then(emailService).should(times(1)).sendEmail(anyString(), any());
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
  }

  @Test
  @DisplayName("본인인증에 성공합니다.")
  void verifySentCode_success() {
    // given
    String email = "example@example.com";
    String code = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 본인인증 메일 전송
    String verificationId = requestSendVerificationMail(email, firstSendTime, code).body().jsonPath().get("result.key");

    // when
    ExtractableResponse<Response> response = requestVerifySentCode(email, code, verificationId);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Verification verification = verificationRepository.findByContact(email)
        .orElseThrow(() -> new RuntimeException());
    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
  }

  @Test
  @DisplayName("verificationId 가 일치하지 않아 본인 인증에 실패합니다.")
  void verifySentCode_fail_because_verificationId_incorrect() {
    // given
    String email = "example@example.com";
    String code = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 본인인증 메일 전송
    String verificationId = requestSendVerificationMail(email, firstSendTime, code).body().jsonPath().get("result.key");
    String incorrectId = "incorrect_id";

    // when
    ExtractableResponse<Response> response = requestVerifySentCode(email, code, incorrectId);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("인증번호가 맞지 않아 본인인증에 실패하고 시도횟수를 1 증가시킵니다.")
  void verifySentCode_fail_because_code_not_match() {
    // given
    String email = "example@example.com";
    String rightCode = "000000";
    LocalDateTime firstSendTime = LocalDateTime.of(2024, 1, 1, 0, 0);
    // 본인인증 메일 전송
    String verificationId = requestSendVerificationMail(email, firstSendTime, rightCode).body().jsonPath().get("result.key");
    String wrongCode = "000011";

    // when
    ExtractableResponse<Response> response = requestVerifySentCode(email, wrongCode, verificationId);

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
    String verificationId = requestSendVerificationMail(email, firstSendTime, rightCode).body().jsonPath().get("result.key");
    String wrongCode = "000011";
    requestVerifySentCode(email, wrongCode, verificationId); // try 1
    requestVerifySentCode(email, wrongCode, verificationId); // try 2
    requestVerifySentCode(email, wrongCode, verificationId); // try 3

    // when
    ExtractableResponse<Response> response = requestVerifySentCode(email, wrongCode, verificationId);

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
        .agreedTermIds(List.of(privateInfoTerm.getId(), marketingTerm.getId()))
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
    assertThat(member.getType()).isEqualTo(MemberType.CLIENT);
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
        List.of(privateInfoTerm.getId(), marketingTerm.getId()));

    sendMailAndValidComplete(request1.getEmail());
    requestSignUp(request1);

    // 중복된 아이디 회원가입
    SignUpRequest request2 = new SignUpRequest("username1", "email2@example.com", "password1", "password1",
        List.of(privateInfoTerm.getId(), marketingTerm.getId()));
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
        List.of(privateInfoTerm.getId(), marketingTerm.getId()));

    sendMailAndValidComplete(request1.getEmail());
    requestSignUp(request1);

    // 중복된 아이디 회원가입
    SignUpRequest request2 = new SignUpRequest("username2", "email1@example.com", "password1", "password1",
        List.of(privateInfoTerm.getId(), marketingTerm.getId()));
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
        List.of(privateInfoTerm.getId(), marketingTerm.getId()));

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
        List.of(marketingTerm.getId())); //필수 약관 미동의

    sendMailAndValidComplete(request.getEmail());

    // when
    ExtractableResponse<Response> response = requestSignUp(request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("회원가입시 보여줄 약관을 조회합니다.")
  void getSignUpTerms_success() {
    //given
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .when().get("/members/sign-up-terms")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<TermDto> terms = response.body().jsonPath().getList("result", TermDto.class);
    assertThat(terms).hasSize(2);
    assertThat(terms).extracting("termId").contains(privateInfoTerm.getId());
    assertThat(terms).extracting("title").contains(kyumallTermDetail.getTitle());
    assertThat(terms).extracting("termId").contains(marketingTerm.getId());
    assertThat(terms).extracting("title").contains(marketingTermDetail2.getTitle());  // 최신 약관 제목
  }

  @Test
  @DisplayName("이메일로 회원 아이디 찾기에 성공합니다.")
  void findUsername_success() {
    // given
    Member member = createMember("username1", "email@example.com", "password11");

    // when
    ExtractableResponse<Response> response = requestFindUsername(member.getEmail());

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    String username =  response.body().jsonPath().get("result.username");
    assertThat(username).isEqualTo(member.getUsername());
  }



  @Test
  @DisplayName("이메일에 해당하는 회원이 없어서 아이디 찾기에 실패합니다.")
  void findUsername_fail_because_email_not_found() {
    // given
    Member member = createMember("username1", "email@example.com", "password11");

    // when
    ExtractableResponse<Response> response = requestFindUsername("incorrect_email@example.com");

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("임시 비밀번호 발급에 성공합니다.")
  void recoverPassword_success() {
    // given
    Member member = createMember("username1", "email@example.com", "password11");
    String tempPassword = "12345678";
    given(randomCodeGenerator.generatePassword()).willReturn(tempPassword);

    // when
    ExtractableResponse<Response> response = requestRecoverPassword(member.getUsername(), member.getEmail());

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Member updatedMember = memberRepository.findByUsername(member.getUsername())
        .orElseThrow(() -> new RuntimeException());
    assertThat(updatedMember.getPassword()).isEqualTo(tempPassword);
  }

  @Test
  @DisplayName("이메일과 username 이 존재하지 않아, 임시 비밀번호 발급에 실패합니다.")
  void recoverPassword_fail_because_email_or_username_not_exists() {
    // given
    Member member = createMember("username1", "email@example.com", "password11");

    // when
    ExtractableResponse<Response> response = requestRecoverPassword("wrongUsername", "wrong@example.com");

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("비밀번호 재설성에 성공합니다.")
  void resetPassword_success() {
    // given
    Member member = createMember("username1", "email@example.com", "password11");
    // 임시 비밀번호 생성
    String tempPassword = "12345678";
    given(randomCodeGenerator.generatePassword()).willReturn(tempPassword);
    requestRecoverPassword(member.getUsername(), member.getEmail());

    String newPassword = "newPassword123";
    ResetPasswordRequest request = ResetPasswordRequest.builder()
        .username(member.getUsername())
        .email(member.getEmail())
        .password(tempPassword)
        .newPassword(newPassword)
        .newPasswordConfirm(newPassword).build();

    // when
    ExtractableResponse<Response> response = requestResetPasswordRequest(request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Member updatedMember = memberRepository.findByUsername(member.getUsername())
        .orElseThrow(() -> new RuntimeException());
    assertThat(updatedMember.getPassword()).isEqualTo(newPassword);
  }

  @Test
  @DisplayName("비밀번호가 일치하지 않아 비밀번호 재설성에 실패합니다.")
  void resetPassword_fail_because_password_not_matched() {
    // given
    Member member = createMember("username1", "email@example.com", "password11");
    // 임시 비밀번호 생성
    String tempPassword = "12345678";
    given(randomCodeGenerator.generatePassword()).willReturn(tempPassword);
    requestRecoverPassword(member.getUsername(), member.getEmail());

    String newPassword = "newPassword123";
    ResetPasswordRequest request = ResetPasswordRequest.builder()
        .username(member.getUsername())
        .email(member.getEmail())
        .password("wrongPassword")  // 틀린 비밀번호
        .newPassword(newPassword)
        .newPasswordConfirm(newPassword).build();

    // when
    ExtractableResponse<Response> response = requestResetPasswordRequest(request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }


  public Member createMember(String username, String email, String password) {
    sendMailAndValidComplete(email);

    SignUpRequest request = SignUpRequest.builder()
        .username(username)
        .email(email)
        .password(password)
        .passwordCheck(password)
        .agreedTermIds(List.of(privateInfoTerm.getId(), marketingTerm.getId())).build();
    requestSignUp(request);
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException());
  }

  /*
  비밀번호 재설정 요청을 보냅니다.
  */
  private static ExtractableResponse<Response> requestResetPasswordRequest(ResetPasswordRequest request) {
    return RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/members/reset-password")
        .then().log().all()
        .extract();
  }

  /*
  임시 비밀번호 설정 요청을 보냅니다.
  */
  private static ExtractableResponse<Response> requestRecoverPassword(String username, String email) {
    RecoverPasswordRequest request = new RecoverPasswordRequest(username, email);

    return RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/members/recover-password")
        .then().log().all()
        .extract();
  }

  /*
  findUsername 요청을 보냅니다.
   */
  private static ExtractableResponse<Response> requestFindUsername(String email) {
    return RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .queryParam("email", email)
        .when().post("/members/find-username")
        .then().log().all()
        .extract();
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
    String verificationId = requestSendVerificationMail(email, sendTime, code).body().jsonPath().get("result.key");
    requestVerifySentCode(email, code, verificationId);
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
  private static ExtractableResponse<Response> requestVerifySentCode(String email, String code, String verificationId) {
    VerifySentCodeRequest request = new VerifySentCodeRequest(email, code, verificationId);

    return RestAssured.given().log().all()
        .body(request).contentType(ContentType.JSON)
        .when().post("/members/verify-sent-code")
        .then().log().all()
        .extract();
  }
}
