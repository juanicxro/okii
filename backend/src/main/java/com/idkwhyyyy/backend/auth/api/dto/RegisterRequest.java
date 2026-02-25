package com.idkwhyyyy.backend.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 190) String email, @NotBlank @Size(min = 6, max = 120) String password) {}
