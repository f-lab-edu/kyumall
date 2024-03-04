package com.kyumall.kyumallcommon.exception;

import com.kyumall.kyumallcommon.response.ResponseWrapper;
import com.kyumall.kyumallcommon.response.ResponseWrapper.BindingError;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * {@link KyumallException} 의 에러처리를 합니다.
   * @param ex
   * @return
   */
  @ExceptionHandler(KyumallException.class)
  public ResponseEntity<ResponseWrapper<Void>> handleKyumallException(KyumallException ex) {
    log.info("## info: {}, {}", ex.getClass().getSimpleName(), ex);
    return ResponseWrapper.fail(ex);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseWrapper<List<BindingError>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    log.info("## info: {}, {}", ex.getClass().getSimpleName(), ex);
    return  ResponseWrapper.fail(
        ex.getBindingResult());
  }

  /**
   * Exception 의 에러처리를 합니다.
   * 상세 에러 메세지를 숨기고 '서버 내부 에러' 메세지를 반환합니다.
   * error 로그를 남깁니다.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseWrapper<Void>> handleException(Exception ex) {
    log.error("## error: {}, {}", ex.getClass().getSimpleName(), ex);   // ex 그대로 반환 -> 에러 메세지 상세하게 표시
    return ResponseWrapper.fail();
  }
}
