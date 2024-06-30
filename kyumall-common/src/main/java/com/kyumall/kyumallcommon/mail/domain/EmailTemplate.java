package com.kyumall.kyumallcommon.mail.domain;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 템플릿 엔티티 입니다.
 * subject : 이메일 주제(제목)
 * template : 에메일 내용 템플릿
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
@Getter
@Entity
public class EmailTemplate extends BaseTimeEntity {
  @Id
  private String id;
  private String subject;
  @Lob
  @Column(columnDefinition = "TEXT")
  private String template;

  @Transient
  private EmailTemplateVariables emailTemplateVariables;

  public void setBindingVariables(EmailTemplateVariables emailTemplateVariables) {
    if (emailTemplateVariables == null) {
      return;
    }
    this.emailTemplateVariables = emailTemplateVariables;
  }

  /**
   * 템플릿에 변수를 바인딩 시켜 반환합니다.
   * @return
   */
  // TODO: 반복문 돌 때 마다 새로운 String 객체가 생성되므로, 차후 다른 방법을 찾아볼 것
  public String bindVariableToTemplate() {
    for (String key: emailTemplateVariables.getVariables().keySet()) {
      String value = emailTemplateVariables.getVariables().get(key);
      template = template.replace(String.format("${%s}", key), value);
    }
    return template;
  }

  @Override
  public String toString() {
    return "EmailTemplate{" +
        "id='" + id + '\'' +
        ", subject='" + subject + '\'' +
        ", template='" + template + '\'' +
        '}';
  }
}
