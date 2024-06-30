package com.kyumall.kyumallcommon.mail.domain;

import com.kyumall.kyumallcommon.mail.domain.EmailTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 메세지 객체입니다.
 * to: 받는사람
 * subject: 메일 주제(제목)
 * message: 메일 메세지(본문)
 * bindingVariables: 메일 템플릿에 바인딩될 변수
 */
@Getter
@AllArgsConstructor @Builder
public class EmailMessage {
  private String to;
  private String subject;
  private String message;

  /**
   * 이메일 템플릿으로 메일 제목과 본문을 만듭니다.
   * @param emailTemplate
   */
  public void bindEmailTemplate(EmailTemplate emailTemplate) {
    this.subject = emailTemplate.getSubject();
    this.message = emailTemplate.bindVariableToTemplate();
  }

  @Override
  public String toString() {
    return "EmailMessage{" +
        "to='" + to + '\'' +
        ", subject='" + subject + '\'' +
        ", message='" + message + '\'' +
        '}';
  }
}
