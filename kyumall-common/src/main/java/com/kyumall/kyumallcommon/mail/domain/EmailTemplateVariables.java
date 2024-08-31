package com.kyumall.kyumallcommon.mail.domain;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * 이메일 템플릿에 바인딩 시킬 변수 객체입니다.
 */
@Getter
public class EmailTemplateVariables {
  private Map<String, String> variables;

  public EmailTemplateVariables(Map<String, String> variables) {
    this.variables = variables;
  }

  /**
   * 빈 EmailTemplateVariables 를 반환합니다.
   * @return
   */
  public static EmailTemplateVariables empty() {
    return new EmailTemplateVariables(new HashMap<>());
  }

  /**
   * 빌더 객체 반환
   * @return
   */
  public static EmailTemplateVariablesBuilder builder() {
    return new EmailTemplateVariablesBuilder();
  }

  public static class EmailTemplateVariablesBuilder {
    private final Map<String, String> variables = new HashMap<>();

    /**
     * 변수를 추가합니다.
     * @param key
     * @param value
     * @return
     */
    public EmailTemplateVariablesBuilder addVariable(String key, String value) {
      this.variables.put(key, value);
      return this;
    }

    public EmailTemplateVariables build() {
      return new EmailTemplateVariables(this.variables);
    }
  }
}
