package com.kyumall.kyumallcommon.auth;

import com.kyumall.kyumallcommon.Util.EncryptUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtProvider {

  private String SECRET_KEY;
  private String PUBLIC_KEY;
  private long EXPIRE_TIME;
  private static final SignatureAlgorithm algorithm = SignatureAlgorithm.PS256;

  public JwtProvider(
      @Value("${jwt.secret-key}") String secretKey,
      @Value("${jwt.public-key}") String publicKey,
      @Value("${jwt.expire-time}") long expireTime) {
    this.SECRET_KEY = secretKey;
    this.PUBLIC_KEY = publicKey;
    this.EXPIRE_TIME = expireTime;
  }

  public String generateToken(String username) {
    return Jwts.builder()
        .signWith(EncryptUtil.convertPrivateKeyInStringToKey(SECRET_KEY, algorithm.getFamilyName()))
        .subject(username)
        .expiration(getExpireDate())
        .compact();
  }


  private Date getExpireDate() {
    Date now = new Date();
    return new Date(now.getTime() + EXPIRE_TIME);
  }

  public String resolveToken(HttpServletRequest request) {
    return request.getHeader("Authorization");
  }

  public boolean validateToken(String accessToken) {
    if (accessToken == null) {
      return false;
    }

    try {
      return Jwts.parser()
          .verifyWith(EncryptUtil.convertPublicKeyInStringToKey(PUBLIC_KEY, algorithm.getFamilyName()))
          .build()
          .parseSignedClaims(accessToken)
          .getPayload().getExpiration().after(new Date());
    }
    catch (Exception e) {
      return false;
    }
  }
}
