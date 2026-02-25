package com.idkwhyyyy.backend.auth;

import com.idkwhyyyy.backend.common.error.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

  public UUID currentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw unauthorized();
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UUID uuid) {
      return uuid;
    }

    try {
      if (principal instanceof String userIdRaw) {
        if ("anonymousUser".equals(userIdRaw)) {
          throw unauthorized();
        }
        return UUID.fromString(userIdRaw);
      }
    } catch (IllegalArgumentException exception) {
      throw unauthorized();
    }

    throw unauthorized();
  }

  private ApiException unauthorized() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "unauthorized", "Authentication is required.");
  }
}
