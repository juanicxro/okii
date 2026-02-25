package com.idkwhyyyy.backend.canvas.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CanvasSummaryResponse(UUID id, String name, OffsetDateTime updatedAt) {}

