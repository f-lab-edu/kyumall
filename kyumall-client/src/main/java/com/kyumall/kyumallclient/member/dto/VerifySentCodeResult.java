package com.kyumall.kyumallclient.member.dto;

public enum VerifySentCodeResult {
  SUCCESS,  // 인증 성공
  FAIL,   // 인증 실패
  EXCEED_COUNT; // 인증 실패 & 시도 횟수 초과
}
