package com.kyumall.kyumallclient.member.dto;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
@AllArgsConstructor @Builder
public class SignUpRequest {
  @NotEmpty @Size(min = 5, max = 12, message = "username 은 5 이상 12 이하 길이여야합니다.")
  private String username;
  @NotEmpty @Email
  private String email;
  @NotEmpty @Size(min = 8, max = 22, message = "password 은 8 이상 20 이하 길이여야합니다.")
  private String password;
  @NotEmpty
  private String passwordCheck; // 비밀번호 확인
  private List<TermAndAgree> termAndAgrees;

  public List<Long> extractTermIds() {
    return termAndAgrees.stream().map(TermAndAgree::getTermId).collect(Collectors.toList());
  }

  public Member toEntity() {
    return Member.builder()    // 서비스에는
        .username(username)
        .email(email)
        .password(password)
        .type(MemberType.USER)
        .status(MemberStatus.INUSE)
        .build();
  }
}
