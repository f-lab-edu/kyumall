package com.kyumall.kyumallclient.member;

import com.kyumall.kyumallclient.member.dto.SendVerificationEmailResponse;
import com.kyumall.kyumallcommon.Util.EncryptUtil;
import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallclient.member.dto.FindUsernameResponse;
import com.kyumall.kyumallclient.member.dto.RecoverPasswordRequest;
import com.kyumall.kyumallclient.member.dto.ResetPasswordRequest;
import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallclient.member.dto.TermDto;
import com.kyumall.kyumallclient.member.dto.VerifySentCodeRequest;
import com.kyumall.kyumallcommon.member.vo.VerifyResult;
import com.kyumall.kyumallcommon.Util.RandomCodeGenerator;
import com.kyumall.kyumallcommon.mail.domain.EmailMessage;
import com.kyumall.kyumallcommon.mail.service.EmailService;
import com.kyumall.kyumallcommon.mail.domain.EmailTemplateVariables;
import com.kyumall.kyumallcommon.member.entity.Agreement;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import com.kyumall.kyumallcommon.member.entity.Verification;
import com.kyumall.kyumallcommon.member.repository.AgreementRepository;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
  public static final String SIGNUP_VERIFICATION_EMAIL_TEMPLATE_ID = "SIGNUP_VERIFICATION";
  public static final String TEMPORARY_PASSWORD_EMAIL_TEMPLATE_ID = "TEMPORARY_PASSWORD";
  private final VerificationRepository verificationRepository;
  private final EmailService emailService;
  private final RandomCodeGenerator randomCodeGenerator;
  private final Clock clock;
  private final MemberRepository memberRepository;
  private final TermRepository termRepository;
  private final AgreementRepository agreementRepository;
  private final PasswordService passwordService;

  /**
   * 본인 인증 메일을 발송합니다. 메일 발송 후 발송 내용을 저장합니다. 이미 발송된 메일이 있는 경우, 재발송 가능한지 체크 후 발송합니다.
   * verification ID를 암호화 한 후 반환합니다. 암호화된 ID는 인증을 검증할때 체크되도록 하여 본인인증의 보안을 강화합니다.
   * @param email 본인인증 받을 메일
   * @param secretKey 암호화를 위한 secretKey
   * @param encryptionAlgorithm id 암호화 할 알고리즘
   * @return 생성된 verification 객체의 id 값
   */
  @Transactional
  public SendVerificationEmailResponse sendVerificationEmail(String email, SecretKey secretKey, String encryptionAlgorithm) {
    verificationRepository.findUnverifiedByContact(email)
            .ifPresent(this::processWhenUnverifiedInfoExists);

    Verification verification = Verification.of(email, randomCodeGenerator, clock);

    emailService.sendEmail(SIGNUP_VERIFICATION_EMAIL_TEMPLATE_ID,
        EmailTemplateVariables.builder()
            .addVariable("verificationCode", verification.getCode()).build(),
        EmailMessage.builder()
          .to(email).build());

    Long verificationID = verificationRepository.save(verification).getId();
    return SendVerificationEmailResponse.of(encryptId(verificationID, encryptionAlgorithm, secretKey));
  }

  private String encryptId(Long id, String encryptionAlgorithm, SecretKey secretKey) {
    try {
      return EncryptUtil.encrypt(encryptionAlgorithm, String.valueOf(id), secretKey);
    } catch (Exception e) {
      throw new KyumallException(ErrorCode.FAIL_TO_ENCRYPT, e);
    }
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
   * 인증객체의 ID와 전달받은 ID(decryptedKey)를 값이 동일한지 검증합니다.
   * @param request
   * @return verifyResult 인증결과
   */
  @Transactional
  public VerifyResult verifySentCode(VerifySentCodeRequest request, String decryptedKey) {
    Verification verification = verificationRepository.findUnverifiedByContact(request.getEmail())
        .orElseThrow(() -> new KyumallException(ErrorCode.VERIFICATION_MAIL_NOT_MATCH));

    validateVerificationKey(verification.getId(), decryptedKey);

    return verification.tryToVerify(request.getCode(), clock);
  }

  private void validateVerificationKey(Long verificationId, String decryptedKey) {
    if (verificationId != Long.parseLong(decryptedKey)) {
      throw new KyumallException(ErrorCode.VERIFICATION_MISMATCH_CODE);
    }
  }

  /**
   * 회원가입
   * 회원의 정보를 저장하고, 약관의 동의 내역을 저장합니다.
   * @param request
   */
  @Transactional
  public void signUp(SignUpRequest request) {
    // 회원 저장
    String encryptedPassword = passwordService.encrypt(request.getPassword());
    Member member = memberRepository.save(request.toEntity(encryptedPassword));

    // 약관 동의 내역 저장
    saveAgreementsOfTerms(request, member);
  }

  private void saveAgreementsOfTerms(SignUpRequest request, Member member) {
    List<Term> terms = termRepository.findAllByStatusOrderByOrdering(TermStatus.INUSE);

    List<Agreement> agreementsToSave = new ArrayList<>();

    for (Term term: terms) {
      if (!checkTermInAgreeIds(term, request.getAgreedTermIds())) {
        throw new KyumallException(ErrorCode.REQUIRED_TERM_MUST_AGREED);
      }
      agreementsToSave.add(Agreement.builder()
          .member(member)
          .term(term)
          .agree(true)
          .build());
    }
    // saveAll Agreements
    agreementRepository.saveAll(agreementsToSave);
  }

  boolean checkTermInAgreeIds(Term term, List<Long> agreedIds) {
    for (Long agreedId: agreedIds) {
      if (Objects.equals(term.getId(), agreedId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 현재 '사용중' 인 약관 중, 가장 최신 버전의 약관 상세를 조회합니다.
   * @return 약관 리스트
   */
  public List<TermDto> getSignUpTerms() {
    List<Term> terms = termRepository.findAllByStatusOrderByOrdering(TermStatus.INUSE);

    List<TermDto> termDtos = new ArrayList<>();
    for (Term term: terms) {
      TermDetail latestTermDetail = term.getTermDetails().stream()
          .max(Comparator.comparingInt(TermDetail::getVersion))
          .orElseThrow(() -> new KyumallException(ErrorCode.TERM_DETAIL_NOT_EXISTS));

      termDtos.add(TermDto.of(term, latestTermDetail));
    }
    return termDtos;
  }

  public FindUsernameResponse findUsername(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
    return FindUsernameResponse.of(member.getUsername());
  }

  /**
   * 임시 비밀번호 발급
   * 임시 비밀번호를 이메일로 전송합니다.
   * @param request
   */
  @Transactional
  public void recoverPassword(RecoverPasswordRequest request) {
    Member member = memberRepository.findByUsernameAndEmail(request.getUsername(),
            request.getEmail())
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));

    String newPassword = member.resetRandomPassword(randomCodeGenerator);

    emailService.sendEmail(TEMPORARY_PASSWORD_EMAIL_TEMPLATE_ID,
        EmailTemplateVariables.builder()
            .addVariable("username", member.getUsername())
            .addVariable("temporaryPassword", newPassword)
            .build(),
        EmailMessage.builder()
            .to(member.getEmail())
            .build());
  }

  /**
   * 비밀번호를 재설정합니다.
   * @param request
   */
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
      throw new KyumallException(ErrorCode.PASSWORD_AND_CONFIRM_NOT_EQUALS);
    }
    Member member = memberRepository.findByUsernameAndEmail(request.getUsername(),
            request.getEmail())
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));

    if (!member.verifyPassword(request.getPassword())) {
      throw new KyumallException(ErrorCode.PASSWORD_NOT_MATCHED);
    }

    member.resetPassword(request.getNewPassword());
  }
}
