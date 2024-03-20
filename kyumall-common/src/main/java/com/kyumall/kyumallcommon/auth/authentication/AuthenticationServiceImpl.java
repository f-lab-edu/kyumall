package com.kyumall.kyumallcommon.auth.authentication;


import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인증 역할을 구현한 구현체
 * {@link SimpleUserInput}을 인증을 위한 입력값으로 받습니다.
 */
@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
  private final MemberRepository memberRepository;
  private final PasswordService passwordService;

  /**
   * 회원 정보를 검증합니다.
   * {@link SimpleUserInput} 타입의 입력값을 처리합니다.
   * @param simpleUserInput 인증을 위한 회원정보 입력값
   * @return 인증 완료된 객체
   */
  @Override
  public AuthenticatedUser verifyUser(Object simpleUserInput) {
    if (simpleUserInput instanceof SimpleUserInput) {
      SimpleUserInput userInput = (SimpleUserInput)simpleUserInput;
      Member member = memberRepository.findByUsername(userInput.getUsername())
          .orElseThrow(() -> new KyumallException(ErrorCode.AUTH_USER_NOT_FOUND));

      if (!passwordService.isMath(userInput.getPassword(), member.getPassword())) {
        throw new KyumallException(ErrorCode.AUTH_PASSWORD_NOT_MATCHED);
      }
      return AuthenticatedUser.from(member);

    } else {
      throw new KyumallException(ErrorCode.AUTH_INPUT_TYPE_NOT_MATCHED);
    }
  }
}
