package com.kyumall.kyumallclient.pay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayResult {
  SUCCESS("1000", "성공"),
  LACK_OF_AMOUNT("2000", "잔액부족");

  private final String code;
  private final String message;
}
