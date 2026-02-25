package com.idkwhyyyy.backend.canvas.service;

import com.idkwhyyyy.backend.canvas.api.dto.CanvasEdgeResponse;
import com.idkwhyyyy.backend.canvas.api.dto.CanvasNodeResponse;
import com.idkwhyyyy.backend.canvas.api.dto.CanvasResponse;
import com.idkwhyyyy.backend.canvas.api.dto.CanvasSummaryResponse;
import com.idkwhyyyy.backend.canvas.api.dto.GraphResponse;
import com.idkwhyyyy.backend.canvas.api.dto.NodeDataResponse;
import com.idkwhyyyy.backend.canvas.api.dto.PositionResponse;
import com.idkwhyyyy.backend.persistence.entity.CanvasEdgeEntity;
import com.idkwhyyyy.backend.persistence.entity.CanvasEntity;
import com.idkwhyyyy.backend.persistence.entity.CanvasNodeEntity;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CanvasMapper {

  private static final Comparator<java.time.OffsetDateTime> NULL_SAFE_DATE_ORDER =
      Comparator.nullsLast(Comparator.naturalOrder());

  public CanvasSummaryResponse toSummary(CanvasEntity canvas) {
    return new CanvasSummaryResponse(canvas.getId(), canvas.getName(), canvas.getUpdatedAt());
  }

  public CanvasResponse toResponse(CanvasEntity canvas) {
    List<CanvasNodeResponse> nodes =
        canvas.getNodes().stream()
            .sorted(
                Comparator.comparing(CanvasNodeEntity::getCreatedAt, NULL_SAFE_DATE_ORDER)
                    .thenComparing(CanvasNodeEntity::getNodeKey))
            .map(this::toNode)
            .toList();

    List<CanvasEdgeResponse> edges =
        canvas.getEdges().stream()
            .sorted(
                Comparator.comparing(CanvasEdgeEntity::getCreatedAt, NULL_SAFE_DATE_ORDER)
                    .thenComparing(CanvasEdgeEntity::getEdgeKey))
            .map(this::toEdge)
            .toList();

    return new CanvasResponse(
        canvas.getId(),
        canvas.getName(),
        canvas.getNextNodeIndex(),
        new GraphResponse(nodes, edges),
        canvas.getCreatedAt(),
        canvas.getUpdatedAt());
  }

  private CanvasNodeResponse toNode(CanvasNodeEntity node) {
    return new CanvasNodeResponse(
        node.getNodeKey(),
        node.getType(),
        new NodeDataResponse(node.getTitle(), node.getSubtitle()),
        new PositionResponse(node.getPositionX(), node.getPositionY()));
  }

  private CanvasEdgeResponse toEdge(CanvasEdgeEntity edge) {
    return new CanvasEdgeResponse(
        edge.getEdgeKey(), edge.getSourceKey(), edge.getTargetKey(), edge.isAnimated());
  }
}
