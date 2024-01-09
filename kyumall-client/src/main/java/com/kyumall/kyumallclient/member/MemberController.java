package com.kyumall.kyumallclient.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Void> sendVerificationMail(@RequestParam String email) {
    memberService.sendVerificationEmail(email);
    return ResponseEntity.ok().build();
  }
}
