package com.kyumall.kyumallcommon.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 공통 에러 1000 ~ 1999
  INTERNAL_SERVER_ERROR("1000", "서버 내부 에러입니다."),
  METHOD_ARGS_INVALID("1001", "입력값이 올바르지 않습니다."),
  FAIL_TO_ENCRYPT("1002", "암호화에 실패했습니다."),
  FAIL_TO_DECRYPT("1003", "복호화에 실패했습니다."),
  FAIL_TO_IMAGE_UPLOAD("1004", "이미지 업로드에 실패했습니다."),
  TEMP_IMAGE_ID_NOT_EXISTS("1005", "임시 이미지가 존재하지 않습니다."),
  TEMP_IMAGE_ID_PARTIALLY_NOT_EXISTS("1006", "해당 임시 이미지가 일부 존재하지 않습니다."),


  // 회원파트 에러 2000 ~ 2999
  VERIFICATION_MAIL_CAN_SEND_IN_TERM("2000", "본인인증 메일은 3분 간격으로 전송 가능합니다."),
  VERIFICATION_MAIL_NOT_MATCH("2001", "본인인증 이메일이 일치하지 않습니다."),
  VERIFICATION_FAILED("2002", "본인인증에 실패했습니다."),
  VERIFICATION_EXCEED_TRY_COUNT("2003", "본인인증 시도 횟수를 초과하였습니다."),
  TERM_NOT_EXISTS("2004", "약관이 존재하지 않습니다."),
  REQUIRED_TERM_MUST_AGREED("2005", "필수 약관은 반드시 동의해야합니다."),
  MEMBER_NOT_EXISTS("2006", "회원 정보가 존재하지 않습니다."),
  PASSWORD_AND_CONFIRM_NOT_EQUALS("2007", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
  PASSWORD_NOT_MATCHED("2008", "비밀번호가 일치하지 않습니다."),
  TERM_DETAIL_NOT_EXISTS("2009", "약관 상세가 존재하지 않습니다."),

  // 상품파트 에러 (3000 ~ 3999)
  CATEGORY_NOT_EXISTS("3000", "카테고리가 존재하지 않습니다."),

  // 메인 파트 에러 (4000 ~ 4999)
  BANNER_GROUP_NOT_FOUND("4000", "배너 그룹을 찾을 수 없습니다.");


  private final String code;
  private final String message;
}
