package com.kyumall.kyumallcommon.response;

import static com.kyumall.kyumallcommon.exception.ErrorCode.*;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  public ResponseWrapper(String code, String message, int statusCode) {
    this.code = code;
    this.message = message;
  }

  /**
   * 성공(200) 응답, 반환 결과가 없을 경우
   */
  @ResponseStatus(org.springframework.http.HttpStatus.OK)
  public static ResponseWrapper<Void> ok() {
    return new ResponseWrapper<>("0", null, null);
  }

  /**
   * 성공(200) 응답, 반환결과가 있을 경우
   * @param result 반환 결과
   */
  @ResponseStatus(org.springframework.http.HttpStatus.OK)
  public static <T> ResponseWrapper<T> ok(T result) {
    return new ResponseWrapper<>("0", null, result);
  }

 public static ResponseEntity<ResponseWrapper<Void>> fail(KyumallException kyumallException) {
    return ResponseWrapper.from(kyumallException.getErrorCode());
 }

  public static ResponseEntity<ResponseWrapper<Void>> fail() {
    return ResponseWrapper.from(INTERNAL_SERVER_ERROR);
  }

  public static ResponseEntity<ResponseWrapper<List<BindingError>>> fail(BindingResult bindingResult) {
    return ResponseEntity
        .status(HttpStatus.SC_BAD_REQUEST)
        .body(new ResponseWrapper<>(METHOD_ARGS_INVALID.getCode(), METHOD_ARGS_INVALID.getMessage()
            , bindingResult.getFieldErrors().stream()
            .map(BindingError::from)
            .toList()));
  }

  private static ResponseEntity<ResponseWrapper<Void>> from(ErrorCode errorCode) {
    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(new ResponseWrapper<>(errorCode.getCode(), errorCode.getMessage(), null));
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
