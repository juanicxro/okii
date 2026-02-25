package com.idkwhyyyy.backend.canvas.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record CreateNodeRequest(
    @Size(max = 120) String title, @Size(max = 255) String subtitle, @Valid PositionRequest position) {}

