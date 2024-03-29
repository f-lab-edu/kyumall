package com.kyumall.kyumallcommon.Util;


import static org.assertj.core.api.Assertions.*;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EncryptUtil 테스트")
class EncryptUtilTest {
  @Test
  @DisplayName("비대칭 키 생성에 성공합니다.")
  void generateAsymmetricKey_success() {
    try {
      // given
      KeyPair rsaPair = EncryptUtil.generateAsymmetricKey("RSA");
      // when
      String base64EncodedPublicKey = Base64.getEncoder().encodeToString(rsaPair.getPublic().getEncoded());
      String base64EncodedPrivateKey = Base64.getEncoder().encodeToString(rsaPair.getPrivate().getEncoded());
      // then
      System.out.println("## base64 encoded keypair");
      System.out.println(base64EncodedPublicKey);
      System.out.println("-------------------------");
      System.out.println(base64EncodedPrivateKey);
      assertThat(base64EncodedPublicKey).isNotNull();
      assertThat(base64EncodedPrivateKey).isNotNull();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("없는 알고리즘 입니다.");
    }
  }

  @Test
  @DisplayName("base64로 인코딩된 키를 Key 타입으로 변환합니다.")
  void convert_base64Key_to_Key_success() {
    try {
      // given
      String algorithm = "RSA";
      KeyPair rsaPair = EncryptUtil.generateAsymmetricKey(algorithm);
      String base64EncodedPublicKey = Base64.getEncoder().encodeToString(rsaPair.getPublic().getEncoded());
      String base64EncodedPrivateKey = Base64.getEncoder().encodeToString(rsaPair.getPrivate().getEncoded());
      System.out.println("## base64 encoded keypair");
      System.out.println(base64EncodedPublicKey);
      System.out.println("-------------------------");
      System.out.println(base64EncodedPrivateKey);
      // when
      PrivateKey privateKey = EncryptUtil.convertPrivateKeyInStringToKey(base64EncodedPrivateKey, algorithm);
      PublicKey publicKey = EncryptUtil.convertPublicKeyInStringToKey(base64EncodedPublicKey, algorithm);
      // then
      assertThat(privateKey).isNotNull();
      assertThat(publicKey).isNotNull();
    } catch (Exception e) {
      throw new RuntimeException("테스트 실패");
    }
  }
}
