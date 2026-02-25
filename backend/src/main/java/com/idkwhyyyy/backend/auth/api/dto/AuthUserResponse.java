package com.idkwhyyyy.backend.auth.api.dto;

import java.util.UUID;

public record AuthUserResponse(UUID id, String email) {}
