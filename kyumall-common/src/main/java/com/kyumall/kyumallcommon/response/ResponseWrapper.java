package com.kyumall.kyumallcommon.response;

import lombok.Getter;

/**
 * 통일된 응답 형식
 */
@Getter
public class ResponseWrapper<T> {
  String code;       // 성공: 0, 실패: 에러코드
  String message;    // 응답에 대한 메세지, 메세지가 필요 없을 경우, null 을 반환함
  T result;          // 요청에 대한 응답 결과

  public ResponseWrapper(String code, String message, T result) {
    this.code = code;
    this.message = message;
    this.result = result;
  }

  /**
   * 성공 응답, 반환 결과가 없을 경우
   */
  public static <T> ResponseWrapper<T> success() {
    return new ResponseWrapper<>("0", null, null);
  }

  /**
   * 성공 응답, 반환결과가 있을 경우
   * @param result 반환 결과
   */
  public static <T> ResponseWrapper<T> success(T result) {
    return new ResponseWrapper<>("0", null, result);
  }
}
