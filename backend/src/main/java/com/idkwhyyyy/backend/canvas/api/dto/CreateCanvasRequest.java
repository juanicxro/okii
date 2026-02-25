package com.idkwhyyyy.backend.canvas.api.dto;

import jakarta.validation.constraints.Size;

public record CreateCanvasRequest(@Size(max = 120) String name) {}

