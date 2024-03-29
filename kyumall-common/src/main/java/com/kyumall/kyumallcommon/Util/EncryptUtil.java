package com.kyumall.kyumallcommon.Util;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {
  public static String ENCRYPT_ALGORITHM = "AES";
  public static SecretKey generateKey(String algorithm) throws NoSuchAlgorithmException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
    keyGenerator.init(128);
    return keyGenerator.generateKey();
  }

  public static String encodeKeyToString(SecretKey key) {
    byte[] encodedKeyBytes = key.getEncoded();
    return Base64.getEncoder().encodeToString(encodedKeyBytes);
  }

  public static SecretKey decodeStringToKey(String encodedKey, String algorithm) {
    byte[] decodedKeyBytes = Base64.getDecoder().decode(encodedKey);
    return new SecretKeySpec(decodedKeyBytes, algorithm);
  }

  public static String encrypt(String algorithm, String data, SecretKey secretKey)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] bytes = cipher.doFinal(data.getBytes());
    return Base64.getEncoder().encodeToString(bytes);
  }

  public static String decrypt(String algorithm, String encryptedText, SecretKey secretKey)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
    byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
    return new String(plainText);
  }

  /**
   * 비대칭 키를 생성합니다.
   * @param algorithm
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static KeyPair generateAsymmetricKey(String algorithm) throws NoSuchAlgorithmException {
    KeyPairGenerator keygen = KeyPairGenerator.getInstance(algorithm);
    keygen.initialize(2048);
    return keygen.generateKeyPair();
  }

  /**
   * String 타입의 private Key를 {@link PublicKey} 타입으로 변경합니다.
   * @param stringTypePublicKey
   * @param algorithm
   * @return
   */
  public static PublicKey convertPublicKeyInStringToKey(String stringTypePublicKey, String algorithm) {
    byte[] decodedKey = Base64.getDecoder().decode(stringTypePublicKey);

    X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
    try {
      KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
      return keyFactory.generatePublic(spec);
    } catch (Exception e) {
      throw new KyumallException(ErrorCode.FAIL_TO_CONVERT_STRING_TYPE_KEY_TO_KEY, e);
    }
  }

  /**
   * String 타입의 private Key를 {@link PrivateKey} 타입으로 변경합니다.
   *
   * @param stringTypePrivateKey
   * @param algorithm
   * @return
   */
  public static PrivateKey convertPrivateKeyInStringToKey(String stringTypePrivateKey, String algorithm) {
    byte[] decodedKey = Base64.getDecoder().decode(stringTypePrivateKey);

    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
    try {
      KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
      return keyFactory.generatePrivate(spec);
    } catch (Exception e) {
      throw new KyumallException(ErrorCode.FAIL_TO_CONVERT_STRING_TYPE_KEY_TO_KEY, e);
    }
  }
}
