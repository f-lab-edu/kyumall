package com.kyumall.kyumallclient.member;

import com.kyumall.kyumallclient.exception.ErrorCode;
import com.kyumall.kyumallclient.exception.KyumallException;
import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallclient.member.dto.TermAndAgree;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.mail.MailService;
import com.kyumall.kyumallcommon.member.entity.Agreement;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.repository.AgreementRepository;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
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
  private final MemberRepository memberRepository;
  private final TermRepository termRepository;
  private final AgreementRepository agreementRepository;

  /**
   * 본인 인증 메일을 발송합니다.
   * 메일 발송 후 발송 내용을 저장합니다.
   * 이미 발송된 메일이 있는 경우, 재발송 가능한지 체크 후 발송합니다.
   * @throws IllegalStateException 메일 전송 이력이 있고 메일 쿨타임이 지나지 않은 경우
   * @param email
   */
  @Transactional
  public void sendVerificationEmail(String email) {
    verificationRepository.findUnverifiedByContact(email)
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
    Verification verification = verificationRepository.findUnverifiedByContact(request.getEmail())
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

  /**
   * 회원가입
   * @param request
   */
  @Transactional
  public void signUp(SignUpRequest request) {
    // 회원 저장
    Member member = memberRepository.save(Member.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(request.getPassword())
        .type(MemberType.USER)
        .status(MemberStatus.INUSE)
        .build());

    // 약관 동의 내역 저장
    for (TermAndAgree termAndAgree: request.getTermAndAgrees()) {
      Term term = termRepository.findById(termAndAgree.getTermId())
          .orElseThrow(() -> new KyumallException(ErrorCode.TERM_NOT_EXISTS));
      // 필수 약관 체크
      if (term.isRequired() && !termAndAgree.isAgree()) {
        throw new KyumallException(ErrorCode.REQUIRED_TERM_MUST_AGREED);
      }

      agreementRepository.save(Agreement.builder()
          .member(member)
          .term(term)
          .agree(termAndAgree.isAgree())
          .build());
    }
  }
}
