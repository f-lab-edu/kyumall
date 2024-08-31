package com.kyumall.kyumallcommon.member.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.member.vo.VerificationStatus;
import com.kyumall.kyumallcommon.member.vo.VerifyResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class Verification extends BaseTimeEntity {
  private static final Integer SEND_RESTRICTED_PERIOD = 3;  // 메일 전송 쿨타임
  private static final Integer VALID_TIME_LIMIT = 3 * 60;   // 인증 유효 시간
  private static final Integer MAX_TRY_COUNT = 3;          // 최대 시도 횟수
  private static final int VERIFICATION_CODE_SIZE = 6;     // 인증 코드 길이

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String contact;
  private String code;
  private LocalDateTime sendDateTime;
  @Enumerated(value = EnumType.STRING)
  private VerificationStatus status;
  private Integer tryCount;

  public static Verification of(String contact, RandomCodeGenerator randomCodeGenerator, Clock clock) {
    return Verification.builder()
        .contact(contact)
        .code(generateCode(randomCodeGenerator))
        .sendDateTime(LocalDateTime.now(clock))
        .status(VerificationStatus.UNVERIFIED)
        .tryCount(0)
        .build();
  }

  /**
   * 랜덤 코드를 생성합니다.
   * @param randomCodeGenerator
   * @return
   */
  public static String generateCode(RandomCodeGenerator randomCodeGenerator) {
    return randomCodeGenerator.generateCode(VERIFICATION_CODE_SIZE);
  }

  /**
   * 메일을 보낼 수 있는 상태인지 체크합니다.
   * 이전 메일로 부터 쿨타임이 있습니다.
   * @return 메일 전송 가능여부
   */
  public boolean checkAbleToSend(Clock clock) {
    long minutesPassed = ChronoUnit.MINUTES.between(sendDateTime, LocalDateTime.now(clock));
    return minutesPassed >= SEND_RESTRICTED_PERIOD;
  }

  public void expired() {
    status = VerificationStatus.EXPIRED;
  }

  public void verified() {
    status = VerificationStatus.VERIFIED;
  }

  /**
   * 인증을 시도합니다.
   * @param code
   * @param clock
   * @return 인증 결과
   */
  public VerifyResult tryToVerify(String code, Clock clock) {
    if (!isWithinTimeLimit(clock)) {
      expired();
      return VerifyResult.EXCEED_TIME_LIMIT;
    }
    if (!isUnderTryCount()) {
      expired();
      return VerifyResult.EXCEED_TRY_COUNT;
    }
    if (!isValidCode(code)) {
      increaseTryCount();
      return VerifyResult.MISMATCH_CODE;
    }
    verified();
    return VerifyResult.SUCCESS;
  }

  /**
   * 인증 시도가 가능한지 체크합니다.
   * tryCount 가 3회 이하여야 합니다.
   * @return
   */
  public boolean isUnderTryCount() {
    return this.tryCount < MAX_TRY_COUNT;
  }

  /**
   * 유효시간 이내인지 체크합니다.
   * @param clock
   * @return
   */
  private boolean isWithinTimeLimit(Clock clock) {
    Duration duration = Duration.between(sendDateTime, LocalDateTime.now(clock));
    return duration.toSeconds() < VALID_TIME_LIMIT;
  }

  private boolean isValidCode(String code) {
    return this.code.equals(code);
  }

  public void increaseTryCount() {
    tryCount += 1;
  }
}
