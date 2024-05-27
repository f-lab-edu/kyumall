package com.kyumall.kyumallclient.member;

import static com.kyumall.kyumallclient.member.MemberService.ID_ENCRYPTION_ALGORITHM;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallclient.member.dto.FindUsernameResponse;
import com.kyumall.kyumallclient.member.dto.RecoverPasswordRequest;
import com.kyumall.kyumallclient.member.dto.ResetPasswordRequest;
import com.kyumall.kyumallclient.member.dto.SendVerificationEmailResponse;
import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallclient.member.dto.TermDto;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeResult;
import com.kyumall.kyumallclient.member.validator.SignUpRequestValidator;
import com.kyumall.kyumallcommon.response.ResponseWrapper;
import com.kyumall.kyumallcommon.Util.EncryptUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/members")
@RestController
public class MemberController {
  @Value("${encrypt.key}")
  private String encryptKey;
  private final MemberService memberService;
  private final SignUpRequestValidator signUpRequestValidator;

  @InitBinder("signUpRequest")  // 검증 하고자 하는 객체의 이름
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.addValidators(signUpRequestValidator);
  }

  /**
   * 본인확인 메일을 전송합니다.
   * @param email
   * @return 생성된 키 값을 암호화 하여 반환합니다.
   */
  @Operation(summary = "본인 확인 메일 전송", description = "이메일 주소로 본인인증 코드를 전송합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "전송 성공", content = { @Content(mediaType = "application/json") }),
      @ApiResponse(responseCode = "400", description = "이메일 형식이 정상적이지 않습니다.")
  })
  @PostMapping("/send-verification-mail")
  public ResponseWrapper<SendVerificationEmailResponse> sendVerificationMail(@NotEmpty @Parameter(description = "인증하려는 이메일", required = true, example = "example@example.com") @RequestParam String email) {
    SecretKey secretKey = EncryptUtil.decodeStringToKey(encryptKey, ID_ENCRYPTION_ALGORITHM);
    Long newId = memberService.sendVerificationEmail(email, secretKey);
    try {
      return ResponseWrapper.ok(SendVerificationEmailResponse.of(
          EncryptUtil.encrypt(ID_ENCRYPTION_ALGORITHM, String.valueOf(newId), secretKey)));
    } catch (Exception e) {
      log.error(e.toString());
      throw new KyumallException(ErrorCode.FAIL_TO_ENCRYPT);
    }
  }

  /**
   * 본인인증 코드와 일치하는지 검증합니다.
   * @param request
   * @return
   */
  @Operation(summary = "본인인증코드 검증", description = "본인인증 코드와 일치하는지 검증합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "본인인증성공"),
      @ApiResponse(responseCode = "400", description = "본인인증에 실패했습니다 / 본인인증 시도 횟수를 초과하였습니다")
  })
  @PostMapping("/verify-sent-code")
  public void verifySentCode(@RequestBody VerifySentCodeRequest request) {
    String decryptKey = decryptKey(request.getVerificationKey());

    VerifySentCodeResult result = memberService.verifySentCode(request, decryptKey);
    // 트랜잭션 내에서 exception을 발생시키면 트랜잭션이 롤백 되어서 밖에서 처리하였습니다.
    if (result == VerifySentCodeResult.FAIL) {
      throw new KyumallException(ErrorCode.VERIFICATION_FAILED);
    }
    if (result == VerifySentCodeResult.EXCEED_COUNT) {
      throw new KyumallException(ErrorCode.VERIFICATION_EXCEED_TRY_COUNT);
    }
  }

  private String decryptKey(String verificationKey) {
    String decryptId;
    try { // 암호화 해제
      SecretKey secretKey = EncryptUtil.decodeStringToKey(encryptKey, ID_ENCRYPTION_ALGORITHM);
      decryptId = EncryptUtil.decrypt(ID_ENCRYPTION_ALGORITHM, verificationKey, secretKey);
    } catch (Exception e) {
      log.error(e.toString());
      throw new KyumallException(ErrorCode.VERIFICATION_FAILED);
    }
    return decryptId;
  }

  /**
   * 회원가입
   * @param request
   */
  @Operation(summary = "회원가입", description = "회원가입 요청을 보냅니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "입력값이 올바르지 않습니다.")
  })
  @PostMapping("/sign-up")
  public void signUp(@RequestBody @Valid SignUpRequest request) {
    memberService.signUp(request);
  }

  /**
   * 회원가입에 필요한 약관을 조회합니다.
   * @return
   */
  @GetMapping("/sign-up-terms")
  public ResponseWrapper<List<TermDto>> getSignUpTerms() {
    return ResponseWrapper.ok(memberService.getSignUpTerms());
  }

  /**
   * 이메일로 아이디 찾기
   * @param email
   * @return
   */
  @PostMapping("/find-username")
  public ResponseWrapper<FindUsernameResponse> findUsername(@RequestParam String email) {
    return ResponseWrapper.ok(memberService.findUsername(email));
  }

  /**
   * 임시 비밀번호 발급
   * @param request
   */
  @PostMapping("/recover-password")
  public void recoverPassword(@RequestBody RecoverPasswordRequest request) {
    memberService.recoverPassword(request);
  }

  /**
   * 비밀번호 재설정
   * @param request
   */
  @PostMapping("/reset-password")
  public void resetPassword(@RequestBody ResetPasswordRequest request) {
    memberService.resetPassword(request);
  }
}
