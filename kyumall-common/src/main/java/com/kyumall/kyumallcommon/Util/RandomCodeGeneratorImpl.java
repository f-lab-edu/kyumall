package com.kyumall.kyumallcommon.Util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomCodeGeneratorImpl implements RandomCodeGenerator {
  private static final int VERIFICATION_CODE_SIZE = 6;

  SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generateCode() {
    return generateCode(VERIFICATION_CODE_SIZE);
  }

  @Override
  public String generateCode(int size) {
    StringBuilder sb = new StringBuilder(size);
    for (int i = 0; i < size; i++) {
      // 0부터 9까지의 난수를 생성하여 문자열로 추가
      sb.append(secureRandom.nextInt(10));
    }
    return sb.toString();
  }
}
