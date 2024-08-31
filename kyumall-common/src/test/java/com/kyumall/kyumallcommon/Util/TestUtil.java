package com.kyumall.kyumallcommon.Util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestUtil {

  /**
   * LocalDateTime 타입의 datetime 을 Clock 타입으로 변환합니다.
   * LocalDateTime.now(Clock) 을 모킹하여 원하는 시간을 반환하도록 할 수 있습니다.
   * LocalDateTime.now(Clock clock) 에서 사용되는 clock.instant() 과 clock.getZone() 를 모킹합니다.
   * @param localDateTime
   * @return
   */
  public static Clock convertLocalDateTimeToClock(LocalDateTime localDateTime) {
    return Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
  }
}
