package com.idkwhyyyy.backend.canvas.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NodeChangeRequest(
    @NotBlank @Size(max = 20) String type,
    @NotBlank @Size(max = 80) String id,
    @Valid PositionRequest position) {}

