package com.kyumall.kyumallcommon.member.vo;


public enum VerifyResult {
  SUCCESS,  // 인증 성공
  EXCEED_TIME_LIMIT,   // 유효시간 초과
  EXCEED_TRY_COUNT, // 시도 횟수 초과
  MISMATCH_CODE; // 코드 불일치
}
