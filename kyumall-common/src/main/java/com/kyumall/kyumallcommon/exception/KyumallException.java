package com.kyumall.kyumallcommon.exception;

public class KyumallException extends RuntimeException {
  private ErrorCode errorCode;
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

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
