package com.idkwhyyyy.backend.auth.service;

import com.idkwhyyyy.backend.auth.api.dto.AuthResponse;
import com.idkwhyyyy.backend.auth.api.dto.AuthUserResponse;
import com.idkwhyyyy.backend.auth.api.dto.LoginRequest;
import com.idkwhyyyy.backend.auth.api.dto.RegisterRequest;
import com.idkwhyyyy.backend.common.error.ApiException;
import com.idkwhyyyy.backend.persistence.entity.UserEntity;
import com.idkwhyyyy.backend.persistence.repository.UserRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService authTokenService;

  public AuthService(
      UserRepository userRepository, PasswordEncoder passwordEncoder, AuthTokenService authTokenService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authTokenService = authTokenService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String normalizedEmail = normalizeEmail(request.email());

    if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
      throw new ApiException(HttpStatus.CONFLICT, "email_already_exists", "Email is already registered.");
    }

    UserEntity user = new UserEntity();
    user.setId(UUID.randomUUID());
    user.setEmail(normalizedEmail);
    user.setPasswordHash(passwordEncoder.encode(request.password()));

    UserEntity persisted = userRepository.save(user);
    return buildAuthResponse(persisted);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    String normalizedEmail = normalizeEmail(request.email());
    UserEntity user =
        userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(this::invalidCredentials);

    if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw invalidCredentials();
    }

    return buildAuthResponse(user);
  }

  private AuthResponse buildAuthResponse(UserEntity user) {
    String token = authTokenService.issue(user.getId());
    return new AuthResponse(token, new AuthUserResponse(user.getId(), user.getEmail()));
  }

  private String normalizeEmail(String rawEmail) {
    return rawEmail.trim().toLowerCase(Locale.ROOT);
  }

  private ApiException invalidCredentials() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials", "Invalid email or password.");
  }
}
