package com.kyumall.kyumallclient.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class FindUsernameResponse {
  private String key;

  public static FindUsernameResponse of(String key) {
    return new FindUsernameResponse(key);
  }
}
