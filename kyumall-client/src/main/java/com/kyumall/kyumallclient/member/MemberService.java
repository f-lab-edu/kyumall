package com.kyumall.kyumallclient.member;

import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.mail.MailService;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
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
  public void sendVerificationEmail(String email) {
    Optional<Verification> unverified = verificationRepository.findUnverifiedByEmail(email);
    if (unverified.isPresent()) {
      processWhenUnverifiedInfoExists(unverified.get());
    }
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
      throw new IllegalStateException("본인 확인 메일은 3분 간격으로 전송 가능합니다.");
    }
    unverifiedVerification.expired();
  }
}
