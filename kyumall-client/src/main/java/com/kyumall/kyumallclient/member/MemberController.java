package com.kyumall.kyumallclient.member;

import com.kyumall.kyumallclient.exception.ErrorCode;
import com.kyumall.kyumallclient.exception.KyumallException;
import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallclient.member.validator.SignUpRequestValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/members")
@RestController
public class MemberController {
  private final MemberService memberService;
  private final SignUpRequestValidator signUpRequestValidator;

  @InitBinder("signUpRequest")  // 검증 하고자 하는 객체의 이름
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.addValidators(signUpRequestValidator);
  }

  /**
   * 본인확인 메일을 전송합니다.
   * @param email
   * @return
   */
  @PostMapping("/send-verification-mail")
  public void sendVerificationMail(@RequestParam String email) {
    memberService.sendVerificationEmail(email);
  }

  /**
   * 본인인증 코드와 일치하는지 검증합니다.
   * @param request
   * @return
   */
  @PostMapping("/verify-sent-code")
  public void verifySentCode(@RequestBody VerifySentCodeRequest request) {
    String result = memberService.verifySentCode(request);
    // 트랜잭션 내에서 exception을 발생시키면 트랜잭션이 롤백 되어서 밖에서 처리하였습니다.
    if (result.equals("FAIL")) {
      throw new KyumallException(ErrorCode.VERIFICATION_FAILED);
    } else if (result.equals("EXCEED_COUNT")) {
      throw new KyumallException(ErrorCode.VERIFICATION_EXCEED_TRY_COUNT);
    }
  }

  /**
   * 회원가입
   * @param request
   */
  @PostMapping("/sign-up")
  public void signUp(@RequestBody @Valid SignUpRequest request) {
    memberService.signUp(request);
  }
}
