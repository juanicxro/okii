package com.idkwhyyyy.backend.canvas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EdgeChangeRequest(
    @NotBlank @Size(max = 20) String type, @NotBlank @Size(max = 120) String id) {}

