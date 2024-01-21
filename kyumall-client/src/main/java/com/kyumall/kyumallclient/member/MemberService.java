package com.kyumall.kyumallclient.member;

import com.kyumall.kyumallclient.exception.ErrorCode;
import com.kyumall.kyumallclient.exception.KyumallException;
import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallclient.member.dto.TermAndAgree;
import com.kyumall.kyumallclient.member.dto.TermDto;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeResult;
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
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
   * 예외 발생 시 Tx Rollback 되기 때문에 실패시, 예외를 트랜잭션 안에서 던지지 않고 결과를 enum 으로 반환합니다.
   * @param request
   * @return VerifySentCodeResult 인증결과
   */
  @Transactional
  public VerifySentCodeResult verifySentCode(VerifySentCodeRequest request) {
    Verification verification = verificationRepository.findUnverifiedByContact(request.getEmail())
        .orElseThrow(() -> new KyumallException(ErrorCode.VERIFICATION_MAIL_NOT_MATCH));
    // 인증 성공
    if (verification.verify(request.getCode())) {
      return VerifySentCodeResult.SUCCESS;
    }
    // 인증 실패
    if (verification.isUnderTryCount()) { // 시도 횟수 3회 미만
      verification.increaseTryCount();
      return VerifySentCodeResult.FAIL;
    }
    // 시도횟수 초과
    verification.expired();
    return VerifySentCodeResult.EXCEED_COUNT;
  }

  /**
   * 회원가입
   * 회원의 정보를 저장하고, 약관의 동의 내역을 저장합니다.
   * @param request
   */
  @Transactional
  public void signUp(SignUpRequest request) {
    // 회원 저장
    Member member = memberRepository.save(request.toEntity());

    // 약관 동의 내역 저장
    saveAgreementsOfTerms(request, member);
  }

  private void saveAgreementsOfTerms(SignUpRequest request, Member member) {
    List<Long> termIds = request.extractTermIds();
    List<Term> terms = termRepository.findAllByIdIn(termIds);

    List<Agreement> agreementsToSave = new ArrayList<>();
    for (TermAndAgree termAndAgree: request.getTermAndAgrees()) {
      Term term = findTermByIdFromList(terms, termAndAgree.getTermId())
          .orElseThrow(() -> new KyumallException(ErrorCode.TERM_NOT_EXISTS));

      validRequiredTermAgreed(termAndAgree, term);

      agreementsToSave.add(Agreement.builder()
          .member(member)
          .term(term)
          .agree(termAndAgree.isAgree())
          .build());
    }
    // saveAll Agreements
    agreementRepository.saveAll(agreementsToSave);
  }

  /**
   * 필수약관 동의 했는지 체크
   */
  private static void validRequiredTermAgreed(TermAndAgree termAndAgree, Term term) {
    if (term.isRequired() && !termAndAgree.isAgree()) {
      throw new KyumallException(ErrorCode.REQUIRED_TERM_MUST_AGREED);
    }
  }

  /**
   * Term List 중 termId를 가지는 Term 을 찾아 반환합니다.
   * @param terms
   * @param termId
   * @return
   */
  private Optional<Term> findTermByIdFromList(List<Term> terms, Long termId) {
    for (Term term: terms) {
      if (Objects.equals(term.getId(), termId)) {
        return Optional.of(term);
      }
    }
    return Optional.empty();
  }

  /**
   * 현재 '사용중' 상태인 모든 약관을 조회합니다.
   */
  public List<TermDto> getSignUpTerms() {
    return termRepository.findAllTermsInUse()
        .stream().map(TermDto::from).toList();
  }
}
