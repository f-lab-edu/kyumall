package com.kyumall.kyumallclient.member.validator;


import com.kyumall.kyumallclient.member.dto.SignUpRequest;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.repository.VerificationRepository;
import com.kyumall.kyumallcommon.member.vo.VerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component @RequiredArgsConstructor
public class SignUpRequestValidator implements Validator {
  private final MemberRepository memberRepository;
  private final VerificationRepository verificationRepository;

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(SignUpRequest.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    SignUpRequest request = (SignUpRequest) target;
    if (memberRepository.existsByUsername(request.getUsername())) {
      errors.rejectValue("username", "invalid.username",
          new Object[]{request.getUsername()}, "이미 사용중인 아이디 입니다.");
    }

    if (memberRepository.existsByEmail(request.getEmail())) {
      errors.rejectValue("email", "invalid.email",
          new Object[]{request.getEmail()}, "이미 사용중인 이메일 입니다.");
    }

    if (!verificationRepository.existsByContactAndStatus(request.getEmail(), VerificationStatus.VERIFIED)) {
      errors.rejectValue("email", "invalid.email",
          new Object[]{request.getEmail()}, "본인 인증 되지 않은 이메일 입니다.");
    }

    if (!request.getPassword().equals(request.getPasswordCheck())) {
      errors.rejectValue("passwordCheck", "invalid.passwordCheck",
          new Object[]{request.getPasswordCheck()}, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }
  }
}
