package com.kyumall.kyumallcommon.mail.service;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.mail.domain.EmailMessage;
import com.kyumall.kyumallcommon.mail.domain.EmailTemplateVariables;
import com.kyumall.kyumallcommon.mail.domain.EmailTemplate;
import com.kyumall.kyumallcommon.mail.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 실제 메일을 발송하지 않고, 콘솔 로그로 출력합니다.
 * local 환경과 test 환경에서 메일 전송 시 해당 서비스가 주입됩니다.
 */
@Slf4j
@Profile({"local", "test"})
@RequiredArgsConstructor
@Component
public class ConsoleEmailService implements EmailService {

  private final EmailTemplateRepository emailTemplateRepository;

  @Override
  public void sendEmail(EmailMessage emailMessage) {
    log.info("sendEmail, emailMessage: {}", emailMessage);
  }

  @Override
  public void sendEmail(String templateId, EmailTemplateVariables emailTemplateVariables, EmailMessage emailMessage) {
    EmailTemplate emailTemplate = emailTemplateRepository.findById(templateId)
        .orElseThrow(() -> new KyumallException(ErrorCode.EMAIL_TEMPLATE_NOT_FOUND));
    emailTemplate.setBindingVariables(emailTemplateVariables);
    emailMessage.bindEmailTemplate(emailTemplate);

    log.info("sendEmail, templateId:{}, emailMessage: {}", templateId, emailMessage);
  }
}
