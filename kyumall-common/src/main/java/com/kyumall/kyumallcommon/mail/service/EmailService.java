package com.kyumall.kyumallcommon.mail.service;


import com.kyumall.kyumallcommon.mail.domain.EmailMessage;
import com.kyumall.kyumallcommon.mail.domain.EmailTemplateVariables;

public interface EmailService {

  /**
   * 이메일 메세지 객체로 메일을 전송합니다.
   * to (받는사람), subject(주제), message(본문)을 전부 직접 명시합닌다.
   * @param emailMessage 이메일 메세지 객체
   */
  void sendEmail(EmailMessage emailMessage);

  /**
   * 이메일 템플릿을 사용하여 메일을 전송합니다.
   * subject(주제)와 message(본문)은 템플릿에 정의된 내용으로 결정됩니다.
   * @param templateId 이메일 템플릿 ID
   * @param emailTemplateVariables 템플릿의 변수와 바인딩 될 값들
   * @param emailMessage 이메일 메세지 객체
   */
  void sendEmail(String templateId, EmailTemplateVariables emailTemplateVariables, EmailMessage emailMessage);
}
