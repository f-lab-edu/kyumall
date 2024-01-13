package com.kyumall.kyumallclient.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 공통 에러 1000 ~ 1999
  INTERNAL_SERVER_ERROR("1000", "[1000] 서버 내부 에러입니다."),

  // 회원파트 에러 2000 ~ 2999
  VERIFICATION_MAIL_CAN_SEND_IN_TERM("2000", "[2000] 본인 확인 메일은 3분 간격으로 전송 가능합니다.");

  private final String code;
  private final String message;

  /**
   * 메세지로 ErrorCode 를 찾을 수 있도록 Map 으로 만들어 둡니다.
   */
  private static final Map<String, ErrorCode> ERROR_CODE_MAP = new HashMap<>();
  static {
    for (ErrorCode errorCode : ErrorCode.values()) {
      ERROR_CODE_MAP.put(errorCode.message, errorCode);
    }
  }

  /**
   * 에러 메세지에 해당하는 에러코드가 존재하는지 확인합니다.
   * @param message 에러메세지
   * @return
   */
  public static boolean isExists(String message) {
    return ERROR_CODE_MAP.containsKey(message);
  }

  /**
   * 에러 메제지에 해당하는 에러코드 Enum 을 반환합니다.
   * @param message 에러메세지
   * @return
   */
  public static ErrorCode findCode(String message) {
    return ERROR_CODE_MAP.get(message);
  }
}
