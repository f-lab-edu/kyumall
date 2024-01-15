package com.kyumall.kyumallclient.member;

import com.kyumall.kyumallclient.exception.ErrorCode;
import com.kyumall.kyumallclient.exception.KyumallException;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.mail.MailService;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {
  private final VerificationRepository verificationRepository;
  private final MailService mailService;
  private final RandomCodeGenerator randomCodeGenerator;
  private final Clock clock;

  /**
   * 본인 인증 메일을 발송합니다.
   * 메일 발송 후 발송 내용을 저장합니다.
   * 이미 발송된 메일이 있는 경우, 재발송 가능한지 체크 후 발송합니다.
   * @throws IllegalStateException 메일 전송 이력이 있고 메일 쿨타임이 지나지 않은 경우
   * @param email
   */
  @Transactional
  public void sendVerificationEmail(String email) {
    verificationRepository.findUnverifiedByEmail(email)
            .ifPresent(this::processWhenUnverifiedInfoExists);

    mailService.sendMail(email);
    verificationRepository.save(Verification.of(email, randomCodeGenerator, clock));
  }

  /**
   * 이미 발송된 메일 중 미인증된 것이 존재하는 경우의 프로세스입니다.
   * 메일을 보낼 수 있는 상태인지 체크합니다.
   * 메일을 발송할 수 있으면 기존에 발송한 것을 만료처리합니다.
   * @param unverifiedVerification
   */
  private void processWhenUnverifiedInfoExists(Verification unverifiedVerification) {
    if (!unverifiedVerification.checkAbleToSend(clock)) {
      throw new KyumallException(ErrorCode.VERIFICATION_MAIL_CAN_SEND_IN_TERM);
    }
    unverifiedVerification.expired();
  }

  /**
   * 본인인증 코드와 일치하는지 검증합니다.
   * @param request
   * @return String 인증결과
   *    SUCCESS: 인증 성공
   *    FAIL: 인증 실패
   *    EXCEED_COUNT: 인증 실패 & 시도 횟수 초과
   */
  @Transactional
  public String verifySentCode(VerifySentCodeRequest request) {
    Verification verification = verificationRepository.findUnverifiedByEmail(request.getEmail())
        .orElseThrow(() -> new KyumallException(ErrorCode.VERIFICATION_MAIL_NOT_MATCH));
    // 인증 성공
    if (verification.verify(request.getCode())) {
      return "SUCCESS";
    }
    // 인증 실패
    if (verification.isUnderTryCount()) { // 시도 횟수 3회 미만
      verification.increaseTryCount();
      return "FAIL";
      //throw new KyumallException(ErrorCode.VERIFICATION_FAILED);  // 예외 발생 시 Tx Rollback 됨
    }
    // 시도횟수 초과
    verification.expired();
    return "EXCEED_COUNT";
    //throw new KyumallException(ErrorCode.VERIFICATION_EXCEED_TRY_COUNT);
  }
}
