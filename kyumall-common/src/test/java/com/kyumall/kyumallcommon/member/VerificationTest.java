package com.kyumall.kyumallcommon.member;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.Util.ApacheRandomCodeGenerator;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.Util.TestUtil;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.vo.VerifyResult;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class VerificationTest {

  RandomCodeGenerator randomCodeGenerator = new ApacheRandomCodeGenerator();

  @Test
  public void tryToVerify_fail_인증시간초과() {
    LocalDateTime sendTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
    Verification verification = Verification.of("test@example.com", randomCodeGenerator,
        TestUtil.convertLocalDateTimeToClock(sendTime));

    VerifyResult verifyResult = verification.tryToVerify("000000",
        TestUtil.convertLocalDateTimeToClock(sendTime.plusMinutes(3)));

    assertThat(verifyResult).isEqualTo(VerifyResult.EXCEED_TIME_LIMIT);
  }

  @Test
  public void tryToVerify_fail_시도횟수초과() {
    LocalDateTime sendTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
    Verification verification = Verification.of("test@example.com", randomCodeGenerator,
        TestUtil.convertLocalDateTimeToClock(sendTime));
    verification.increaseTryCount();
    verification.increaseTryCount();
    verification.increaseTryCount();

    VerifyResult verifyResult = verification.tryToVerify("000000",
        TestUtil.convertLocalDateTimeToClock(sendTime.plusMinutes(1)));

    assertThat(verifyResult).isEqualTo(VerifyResult.EXCEED_TRY_COUNT);
  }

  @Test
  public void tryToVerify_fail_코드불일치() {
    // Mocking
    RandomCodeGenerator codeGenerator = new RandomCodeGenerator() {
      @Override
      public String generateCode(int size) {
        return "000000";
      }

      @Override
      public String generatePassword() {
        return null;
      }
    };
    LocalDateTime sendTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
    Verification verification = Verification.of("test@example.com", codeGenerator,
        TestUtil.convertLocalDateTimeToClock(sendTime));

    VerifyResult verifyResult = verification.tryToVerify("000001",
        TestUtil.convertLocalDateTimeToClock(sendTime.plusMinutes(1)));

    assertThat(verifyResult).isEqualTo(VerifyResult.MISMATCH_CODE);
  }

  @Test
  public void tryToVerify_success() {
    // Mocking
    RandomCodeGenerator codeGenerator = new RandomCodeGenerator() {
      @Override
      public String generateCode(int size) {
        return "000000";
      }
      @Override
      public String generatePassword() {
        return null;
      }
    };
    LocalDateTime sendTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
    Verification verification = Verification.of("test@example.com", codeGenerator,
        TestUtil.convertLocalDateTimeToClock(sendTime));

    VerifyResult verifyResult = verification.tryToVerify("000000",
        TestUtil.convertLocalDateTimeToClock(sendTime.plusMinutes(1)));

    assertThat(verifyResult).isEqualTo(VerifyResult.SUCCESS);
  }
}
