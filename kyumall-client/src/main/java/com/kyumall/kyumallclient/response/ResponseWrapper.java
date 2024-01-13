package com.kyumall.kyumallclient.response;

import com.kyumall.kyumallclient.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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

  public ResponseWrapper(String code, String message) {
    this.code = code;
    this.message = message;
  }

  /**
   * 성공(200) 응답, 반환 결과가 없을 경우
   */
  @ResponseStatus(HttpStatus.OK)
  public static <T> ResponseWrapper<T> ok() {
    return new ResponseWrapper<>("0", null, null);
  }

  /**
   * 성공(200) 응답, 반환결과가 있을 경우
   * @param result 반환 결과
   */
  @ResponseStatus(HttpStatus.OK)
  public static <T> ResponseWrapper<T> ok(T result) {
    return new ResponseWrapper<>("0", null, result);
  }

  /**
   * Exception 으로 부터 실패 응답 생성
   * {@link com.kyumall.kyumallclient.exception.GlobalExceptionHandler} 에서 호출됩니다.
   * Exception Message 가 {@link ErrorCode}에 존재하는 경우 ErrorCode 로 부터 반환값이 생성됩니다.
   * ErrorCode 에 존재하지 않는 경우, Exception Message 로 부터 반환값 생성됩니다.
   * @param ex
   * @return
   */
  public static ResponseWrapper<Void> fail(Exception ex) {
    if (ErrorCode.isExists(ex.getMessage())) {
      return fromErrorCode(ErrorCode.findCode(ex.getMessage()));
    }
    return fromExceptionMessage(ex.getMessage());
  }

  private static ResponseWrapper<Void> fromErrorCode(ErrorCode errorCode) {
    return new ResponseWrapper<>(errorCode.getCode(), errorCode.getMessage());
  }

  private static ResponseWrapper<Void> fromExceptionMessage(String exceptionMessage) {
    return new ResponseWrapper<>("", exceptionMessage);
  }
}
