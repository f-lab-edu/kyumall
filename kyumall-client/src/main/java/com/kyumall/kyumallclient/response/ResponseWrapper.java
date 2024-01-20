package com.kyumall.kyumallclient.response;

import static com.kyumall.kyumallclient.exception.ErrorCode.*;

import com.kyumall.kyumallclient.exception.ErrorCode;
import com.kyumall.kyumallclient.exception.KyumallException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
  public static ResponseWrapper<Void> ok() {
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

  private static ResponseWrapper<Void> from(ErrorCode errorCode) {
    return new ResponseWrapper<>(errorCode.getCode(), errorCode.getMessage(), null);
  }

 public static ResponseWrapper<Void> fail(KyumallException kyumallException) {
    return ResponseWrapper.from(kyumallException.getErrorCode());
 }

  public static ResponseWrapper<Void> fail() {
    return ResponseWrapper.from(INTERNAL_SERVER_ERROR);
  }

  public static ResponseWrapper<List<BindingError>> fail(BindingResult bindingResult) {
    return new ResponseWrapper<>(METHOD_ARGS_INVALID.getCode(), METHOD_ARGS_INVALID.getMessage()
        , bindingResult.getFieldErrors().stream()
                    .map(BindingError::from)
                    .toList());
  }

  @Getter @AllArgsConstructor
  public static class BindingError {
    private String fieldName;
    private String errorMessage;

    public static BindingError from(FieldError fieldError) {
      return new BindingError(fieldError.getField(), fieldError.getDefaultMessage());
    }

    @Override
    public String toString() {
      return "BindingError{" +
          "fieldName='" + fieldName + '\'' +
          ", errorMessage='" + errorMessage + '\'' +
          '}';
    }
  }
}
