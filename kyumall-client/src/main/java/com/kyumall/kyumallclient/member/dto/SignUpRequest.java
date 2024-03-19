package com.kyumall.kyumallclient.member.dto;

import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
@AllArgsConstructor @Builder
public class SignUpRequest {
  @Schema(description = "아이디", example = "test01")
  @NotEmpty @Size(min = 5, max = 12, message = "username 은 5 이상 12 이하 길이여야합니다.")
  private String username;
  @Schema(description = "이메일", example = "example@example.com")
  @NotEmpty @Email
  private String email;
  @Schema(description = "비밀번호", example = "absde12345")
  @NotEmpty @Size(min = 8, max = 22, message = "password 은 8 이상 20 이하 길이여야합니다.")
  private String password;
  @Schema(description = "비밀번호 확인", example = "absde12345")
  @NotEmpty
  private String passwordCheck; // 비밀번호 확인
  @Schema(description = "동의한 약관 ID", example = "[1, 2]")
  private List<Long> agreedTermIds;

  public Member toEntity(String encodedPassword) {
    return Member.builder()
        .username(username)
        .email(email)
        .password(encodedPassword)
        .type(MemberType.CLIENT)
        .status(MemberStatus.INUSE)
        .build();
  }
}
