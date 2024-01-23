package com.kyumall.kyumallcommon.Util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class RandomCodeGeneratorImpl implements RandomCodeGenerator {
  @Override
  public String generateCode(int size) {
    return RandomStringUtils.randomNumeric(size);
  }

  @Override
  public String generatePassword() {
    return RandomStringUtils.random(8);
  }
}
