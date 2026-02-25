package com.idkwhyyyy.backend.auth.service;

import com.idkwhyyyy.backend.common.error.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

  private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

  private final byte[] secret;
  private final long tokenTtlSeconds;

  public AuthTokenService(
      @Value("${app.auth.secret:dev-change-this-secret}") String secret,
      @Value("${app.auth.token-ttl-seconds:86400}") long tokenTtlSeconds) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.tokenTtlSeconds = tokenTtlSeconds;
  }

  public String issue(UUID userId) {
    long expiresAt = Instant.now().plusSeconds(tokenTtlSeconds).getEpochSecond();
    String userPart = BASE64_URL_ENCODER.encodeToString(userId.toString().getBytes(StandardCharsets.UTF_8));
    String payload = userPart + "." + expiresAt;
    String signature = BASE64_URL_ENCODER.encodeToString(sign(payload));
    return payload + "." + signature;
  }

  public UUID verifyAndExtractUserId(String token) {
    if (token == null || token.isBlank()) {
      throw unauthorized();
    }

    String[] chunks = token.trim().split("\\.");
    if (chunks.length != 3) {
      throw unauthorized();
    }

    String payload = chunks[0] + "." + chunks[1];
    byte[] expectedSignature = sign(payload);
    byte[] receivedSignature;

    try {
      receivedSignature = BASE64_URL_DECODER.decode(chunks[2]);
    } catch (IllegalArgumentException exception) {
      throw unauthorized();
    }

    if (!MessageDigest.isEqual(expectedSignature, receivedSignature)) {
      throw unauthorized();
    }

    long expiresAt;
    try {
      expiresAt = Long.parseLong(chunks[1]);
    } catch (NumberFormatException exception) {
      throw unauthorized();
    }

    if (Instant.now().getEpochSecond() >= expiresAt) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "token_expired", "Authentication token has expired.");
    }

    try {
      String userIdRaw = new String(BASE64_URL_DECODER.decode(chunks[0]), StandardCharsets.UTF_8);
      return UUID.fromString(userIdRaw);
    } catch (IllegalArgumentException exception) {
      throw unauthorized();
    }
  }

  private byte[] sign(String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    } catch (Exception exception) {
      throw new ApiException(
          HttpStatus.INTERNAL_SERVER_ERROR, "token_signing_error", "Could not sign authentication token.");
    }
  }

  private ApiException unauthorized() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "invalid_token", "Invalid authentication token.");
  }
}
