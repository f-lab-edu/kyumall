package com.kyumall.kyumallcommon.Util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
}
