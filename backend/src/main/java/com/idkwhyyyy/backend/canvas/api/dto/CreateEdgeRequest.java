package com.idkwhyyyy.backend.canvas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEdgeRequest(
    @NotBlank @Size(max = 80) String source, @NotBlank @Size(max = 80) String target) {}

