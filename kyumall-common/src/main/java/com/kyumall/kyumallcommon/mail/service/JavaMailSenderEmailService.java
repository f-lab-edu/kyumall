package com.kyumall.kyumallcommon.mail.service;


import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.mail.domain.EmailMessage;
import com.kyumall.kyumallcommon.mail.domain.EmailTemplateVariables;
import com.kyumall.kyumallcommon.mail.domain.EmailTemplate;
import com.kyumall.kyumallcommon.mail.repository.EmailTemplateRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * javaMailSender 를 사용하여 이메일을 발송합니다.
 * application-mail.yml 의 설정값을 주입 받습니다.
 */
@Slf4j
@Profile({"dev"})
@Service
@RequiredArgsConstructor
public class JavaMailSenderEmailService implements EmailService {
  private final JavaMailSender javaMailSender;
  private final EmailTemplateRepository emailTemplateRepository;

  /**
   * 이메일 메세지 객체로 메일을 만들어 전송합니다.
   * @param emailMessage 이메일 메세지 객체
   */
  @Override
  public void sendEmail(EmailMessage emailMessage) {
    log.debug("sendEmail, emailMessage: {}", emailMessage);
    sendMailByJavaMailSender(emailMessage);
  }

  /**
   * 이메일 템플릿으로 메일을 만들어 전송합니다.
   * @param templateId 이메일 템플릿 ID
   * @param emailTemplateVariables 템플릿의 변수와 바인딩 될 값들
   * @param emailMessage 이메일 메세지 객체
   */
  @Override
  public void sendEmail(String templateId, EmailTemplateVariables emailTemplateVariables, EmailMessage emailMessage) {
    log.debug("sendEmail, templateId:{}, emailMessage: {}", templateId, emailMessage);
    EmailTemplate emailTemplate = emailTemplateRepository.findById(templateId)
        .orElseThrow(() -> new KyumallException(ErrorCode.EMAIL_TEMPLATE_NOT_FOUND));
    emailTemplate.setEmailTemplateVariables(emailTemplateVariables);

    emailMessage.bindEmailTemplate(emailTemplate);
    sendMailByJavaMailSender(emailMessage);
  }

  private void sendMailByJavaMailSender(EmailMessage emailMessage) {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    try {
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
      mimeMessageHelper.setTo(emailMessage.getTo());
      mimeMessageHelper.setSubject(emailMessage.getSubject());
      mimeMessageHelper.setText(emailMessage.getMessage(), true);
      javaMailSender.send(mimeMessage);
    } catch (MessagingException e) {
      throw new KyumallException(ErrorCode.EMAIL_SEND_FAIL);
    }
  }
}
