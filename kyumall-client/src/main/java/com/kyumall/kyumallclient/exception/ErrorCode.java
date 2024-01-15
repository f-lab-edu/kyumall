package com.kyumall.kyumallclient.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 공통 에러 1000 ~ 1999
  INTERNAL_SERVER_ERROR("1000", "서버 내부 에러입니다."),

  // 회원파트 에러 2000 ~ 2999
  VERIFICATION_MAIL_CAN_SEND_IN_TERM("2000", "본인인증 메일은 3분 간격으로 전송 가능합니다."),
  VERIFICATION_MAIL_NOT_MATCH("2001", "본인인증 이메일이 일치하지 않습니다."),
  VERIFICATION_FAILED("2002", "본인인증에 실패했습니다."),
  VERIFICATION_EXCEED_TRY_COUNT("2003", "본인인증 시도 횟수를 초과하였습니다.");

  private final String code;
  private final String message;
}
