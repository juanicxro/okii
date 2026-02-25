package com.idkwhyyyy.backend.canvas.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CanvasResponse(
    UUID id,
    String name,
    Integer nextNodeIndex,
    GraphResponse graph,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}

