package com.kyumall.kyumallclient.pay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PayResponse {
  private String result;
  private String message;
}