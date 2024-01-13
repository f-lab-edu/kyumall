package com.kyumall.kyumallcommon.member.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.member.vo.VerificationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Clock;
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
  private static final int VERIFICATION_CODE_SIZE = 6;  // 인증 코드 길이

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
}
