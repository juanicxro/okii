package com.idkwhyyyy.backend.auth.security;

import com.idkwhyyyy.backend.auth.service.AuthTokenService;
import com.idkwhyyyy.backend.common.error.ApiException;
import com.idkwhyyyy.backend.persistence.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

  private final AuthTokenService authTokenService;
  private final UserRepository userRepository;

  public BearerTokenAuthenticationFilter(AuthTokenService authTokenService, UserRepository userRepository) {
    this.authTokenService = authTokenService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authorization.substring("Bearer ".length()).trim();

    try {
      UUID userId = authTokenService.verifyAndExtractUserId(token);

      if (!userRepository.existsById(userId)) {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid_token", "Token user does not exist.");
      }

      UsernamePasswordAuthenticationToken authentication =
          UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (ApiException exception) {
      SecurityContextHolder.clearContext();
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
