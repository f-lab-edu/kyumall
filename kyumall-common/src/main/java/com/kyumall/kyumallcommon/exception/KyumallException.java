package com.kyumall.kyumallcommon.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class KyumallException extends RuntimeException {
  private ErrorCode errorCode;
  private Map<String, Object> parameters = new HashMap<>();

  public KyumallException(String message) {
    super(message);
  }

  public KyumallException(String message, Throwable cause) {
    super(message, cause);
  }

  public KyumallException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public KyumallException(ErrorCode errorCode, Throwable throwable) {
    super(errorCode.getMessage(), throwable);
    this.errorCode = errorCode;
  }

  public KyumallException(ErrorCode errorCode, Map<String, Object> parameters) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.parameters = parameters;
  }
}
