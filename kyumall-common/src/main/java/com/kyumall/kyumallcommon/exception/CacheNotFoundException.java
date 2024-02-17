package com.kyumall.kyumallcommon.exception;

public class CacheNotFoundException extends Exception {
  public CacheNotFoundException(String message) {
    super(message);
  }

  public CacheNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public CacheNotFoundException(Throwable cause) {
    super(cause);
  }
}
