package com.idkwhyyyy.backend.canvas.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PatchNodesRequest(@NotNull @Valid List<NodeChangeRequest> changes) {}

