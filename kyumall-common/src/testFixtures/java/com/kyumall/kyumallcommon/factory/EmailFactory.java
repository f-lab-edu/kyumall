package com.kyumall.kyumallcommon.factory;

import com.kyumall.kyumallcommon.mail.domain.EmailTemplate;
import com.kyumall.kyumallcommon.mail.repository.EmailTemplateRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class EmailFactory {
  @Autowired
  private EmailTemplateRepository emailTemplateRepository;
  @Autowired
  private ResourceLoader resourceLoader;

  public EmailTemplate createEmailTemplate(String templateId, String subject, String fileName) {
    EmailTemplate emailTemplate = loadEmailTemplate(templateId, subject, fileName);
    return emailTemplateRepository.saveAndFlush(emailTemplate);
  }

  private EmailTemplate loadEmailTemplate(String templateId, String subject ,String fileName) {
    try {
      String readFile = getResourceAsString(fileName);
      return EmailTemplate.builder()
          .id(templateId)
          .subject(subject)
          .template(readFile)
          .build();
    } catch (IOException e) {
      throw new RuntimeException("템플릿 로드 중 오류가 발생", e);
    }
  }

  private String getResourceAsString(String fileName) throws IOException {
    Resource resource = resourceLoader.getResource("classpath:" + fileName + ".html");
    try (InputStream inputStream = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
