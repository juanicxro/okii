package com.idkwhyyyy.backend.auth.api.dto;

public record AuthResponse(String token, AuthUserResponse user) {}
