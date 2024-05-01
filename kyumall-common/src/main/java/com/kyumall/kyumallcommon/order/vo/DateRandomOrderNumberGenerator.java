package com.kyumall.kyumallcommon.order.vo;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class DateRandomOrderNumberGenerator implements OrderNumberGenerator {
  SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd"); // 6자리 날짜
  SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmssSSS"); // 9자리 시분초밀리초
  private static final int RANDOM_SIZE = 6;

  @Override
  public String generate() {
    // 현재 날짜 및 시간
    Date now = new Date();

    // 날짜와 시간 포맷에 따라 문자열 생성
    String datePart = dateFormat.format(now);
    String timePart = timeFormat.format(now);

    // 랜덤 5자리
    String randomPart = RandomStringUtils.randomNumeric(RANDOM_SIZE);

    return datePart + timePart + randomPart;
  }
}
