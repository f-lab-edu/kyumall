package com.kyumall.kyumallclient.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString @AllArgsConstructor
public class TermAndAgree {
  private Long termId;
  private boolean agree;
}
