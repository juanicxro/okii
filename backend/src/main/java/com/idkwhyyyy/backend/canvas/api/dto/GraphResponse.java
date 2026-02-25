package com.idkwhyyyy.backend.canvas.api.dto;

import java.util.List;

public record GraphResponse(List<CanvasNodeResponse> nodes, List<CanvasEdgeResponse> edges) {}

