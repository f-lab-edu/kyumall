package com.kyumall.kyumallclient.member;

import com.kyumall.kyumallclient.response.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/members")
@RestController
public class MemberController {
  private final MemberService memberService;

  /**
   * 본인확인 메일을 전송합니다.
   * @param email
   * @return
   */
  @PostMapping("/send-verification-mail")
  public ResponseWrapper<Void> sendVerificationMail(@RequestParam String email) {
    memberService.sendVerificationEmail(email);
    return ResponseWrapper.ok();
  }
}
