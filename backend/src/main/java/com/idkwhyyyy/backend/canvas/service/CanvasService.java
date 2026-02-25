package com.idkwhyyyy.backend.canvas.service;

import com.idkwhyyyy.backend.auth.CurrentUserProvider;
import com.idkwhyyyy.backend.canvas.api.dto.CanvasResponse;
import com.idkwhyyyy.backend.canvas.api.dto.CanvasSummaryResponse;
import com.idkwhyyyy.backend.canvas.api.dto.CreateCanvasRequest;
import com.idkwhyyyy.backend.canvas.api.dto.CreateEdgeRequest;
import com.idkwhyyyy.backend.canvas.api.dto.CreateNodeRequest;
import com.idkwhyyyy.backend.canvas.api.dto.EdgeChangeRequest;
import com.idkwhyyyy.backend.canvas.api.dto.NodeChangeRequest;
import com.idkwhyyyy.backend.canvas.api.dto.PatchEdgesRequest;
import com.idkwhyyyy.backend.canvas.api.dto.PatchNodesRequest;
import com.idkwhyyyy.backend.canvas.api.dto.PositionRequest;
import com.idkwhyyyy.backend.canvas.api.dto.RenameCanvasRequest;
import com.idkwhyyyy.backend.common.error.ApiException;
import com.idkwhyyyy.backend.persistence.entity.CanvasEdgeEntity;
import com.idkwhyyyy.backend.persistence.entity.CanvasEntity;
import com.idkwhyyyy.backend.persistence.entity.CanvasNodeEntity;
import com.idkwhyyyy.backend.persistence.entity.UserEntity;
import com.idkwhyyyy.backend.persistence.repository.CanvasRepository;
import com.idkwhyyyy.backend.persistence.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CanvasService {

  private static final int GRID_COLUMNS = 3;
  private static final double GRID_START_X = 80;
  private static final double GRID_START_Y = 280;
  private static final double GRID_STEP_X = 300;
  private static final double GRID_STEP_Y = 150;

  private final CanvasRepository canvasRepository;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;
  private final CanvasMapper canvasMapper;

  public CanvasService(
      CanvasRepository canvasRepository,
      UserRepository userRepository,
      CurrentUserProvider currentUserProvider,
      CanvasMapper canvasMapper) {
    this.canvasRepository = canvasRepository;
    this.userRepository = userRepository;
    this.currentUserProvider = currentUserProvider;
    this.canvasMapper = canvasMapper;
  }

  @Transactional(readOnly = true)
  public List<CanvasSummaryResponse> listCanvases() {
    UUID userId = currentUserProvider.currentUserId();
    return canvasRepository.findAllByUser_IdOrderByUpdatedAtDesc(userId).stream()
        .map(canvasMapper::toSummary)
        .toList();
  }

  @Transactional(readOnly = true)
  public CanvasResponse getCanvas(UUID canvasId) {
    return canvasMapper.toResponse(getCanvasForCurrentUser(canvasId));
  }

  @Transactional
  public CanvasResponse createCanvas(CreateCanvasRequest request) {
    UserEntity user = resolveCurrentUser();
    int fallbackIndex = (int) canvasRepository.countByUser_Id(user.getId()) + 1;
    String name = normalizeName(request == null ? null : request.name(), "Canvas " + fallbackIndex);

    CanvasEntity canvas = new CanvasEntity();
    canvas.setUser(user);
    canvas.setName(name);
    canvas.setNextNodeIndex(4);

    CanvasNodeEntity start = createNode("start", "action", "Entrada", "Coleta de contexto da tarefa", 80, 120);
    CanvasNodeEntity validate =
        createNode("validate", "action", "Validacao", "Checagem de regras e estrutura", 380, 120);
    CanvasNodeEntity done = createNode("done", "action", "Saida", "Retorno final da execucao", 680, 120);

    canvas.addNode(start);
    canvas.addNode(validate);
    canvas.addNode(done);
    canvas.addEdge(createEdge("start-validate", "start", "validate", true));
    canvas.addEdge(createEdge("validate-done", "validate", "done", true));

    return canvasMapper.toResponse(canvasRepository.saveAndFlush(canvas));
  }

  @Transactional
  public CanvasResponse renameCanvas(UUID canvasId, RenameCanvasRequest request) {
    CanvasEntity canvas = getCanvasForCurrentUser(canvasId);
    canvas.setName(normalizeName(request == null ? null : request.name(), canvas.getName()));
    return canvasMapper.toResponse(canvasRepository.saveAndFlush(canvas));
  }

  @Transactional
  public void deleteCanvas(UUID canvasId) {
    CanvasEntity canvas = getCanvasForCurrentUser(canvasId);
    canvasRepository.delete(canvas);
  }

  @Transactional
  public CanvasResponse createNode(UUID canvasId, CreateNodeRequest request) {
    CanvasEntity canvas = getCanvasForCurrentUser(canvasId);

    int nodeIndex = canvas.getNextNodeIndex();
    String nodeKey = "step-" + nodeIndex;

    PositionRequest requestedPosition = request == null ? null : request.position();
    double x = requestedPosition != null && requestedPosition.x() != null
        ? requestedPosition.x()
        : calculateGridX(nodeIndex);
    double y = requestedPosition != null && requestedPosition.y() != null
        ? requestedPosition.y()
        : calculateGridY(nodeIndex);

    String title =
        normalizeName(request == null ? null : request.title(), "Passo " + nodeIndex);
    String subtitle =
        normalizeName(
            request == null ? null : request.subtitle(), "Novo bloco para seu fluxo");

    CanvasNodeEntity node = createNode(nodeKey, "action", title, subtitle, x, y);
    canvas.addNode(node);

    CanvasNodeEntity previousNode =
        canvas.getNodes().size() > 1 ? canvas.getNodes().get(canvas.getNodes().size() - 2) : null;
    if (previousNode != null) {
      String edgeKey = previousNode.getNodeKey() + "-" + nodeKey;
      if (canvas.getEdges().stream().noneMatch(edge -> edge.getEdgeKey().equals(edgeKey))) {
        canvas.addEdge(createEdge(edgeKey, previousNode.getNodeKey(), nodeKey, true));
      }
    }

    canvas.setNextNodeIndex(nodeIndex + 1);
    return canvasMapper.toResponse(canvasRepository.saveAndFlush(canvas));
  }

  @Transactional
  public CanvasResponse createEdge(UUID canvasId, CreateEdgeRequest request) {
    CanvasEntity canvas = getCanvasForCurrentUser(canvasId);
    String source = request.source().trim();
    String target = request.target().trim();

    if (source.equals(target)) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "invalid_edge",
          "Source and target cannot be the same node.");
    }

    boolean sourceExists = canvas.getNodes().stream().anyMatch(node -> node.getNodeKey().equals(source));
    boolean targetExists = canvas.getNodes().stream().anyMatch(node -> node.getNodeKey().equals(target));
    if (!sourceExists || !targetExists) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "invalid_edge",
          "Source and target must exist in the same canvas.");
    }

    String edgeKey = source + "-" + target;
    boolean edgeExists = canvas.getEdges().stream().anyMatch(edge -> edge.getEdgeKey().equals(edgeKey));
    if (edgeExists) {
      throw new ApiException(HttpStatus.CONFLICT, "edge_exists", "Edge already exists.");
    }

    canvas.addEdge(createEdge(edgeKey, source, target, true));
    return canvasMapper.toResponse(canvasRepository.saveAndFlush(canvas));
  }

  @Transactional
  public CanvasResponse applyNodeChanges(UUID canvasId, PatchNodesRequest request) {
    CanvasEntity canvas = getCanvasForCurrentUser(canvasId);
    if (request == null || request.changes() == null) {
      return canvasMapper.toResponse(canvas);
    }

    for (NodeChangeRequest change : request.changes()) {
      String type = change.type().toLowerCase(Locale.ROOT);
      String nodeKey = change.id();

      if ("remove".equals(type)) {
        boolean removed = canvas.getNodes().removeIf(node -> node.getNodeKey().equals(nodeKey));
        if (removed) {
          canvas.getEdges()
              .removeIf(edge -> edge.getSourceKey().equals(nodeKey) || edge.getTargetKey().equals(nodeKey));
        }
        continue;
      }

      if ("position".equals(type) && change.position() != null) {
        for (CanvasNodeEntity node : canvas.getNodes()) {
          if (!node.getNodeKey().equals(nodeKey)) {
            continue;
          }
          if (change.position().x() != null) {
            node.setPositionX(change.position().x());
          }
          if (change.position().y() != null) {
            node.setPositionY(change.position().y());
          }
          break;
        }
      }
    }

    return canvasMapper.toResponse(canvasRepository.saveAndFlush(canvas));
  }

  @Transactional
  public CanvasResponse applyEdgeChanges(UUID canvasId, PatchEdgesRequest request) {
    CanvasEntity canvas = getCanvasForCurrentUser(canvasId);
    if (request == null || request.changes() == null) {
      return canvasMapper.toResponse(canvas);
    }

    for (EdgeChangeRequest change : request.changes()) {
      if ("remove".equalsIgnoreCase(change.type())) {
        canvas.getEdges().removeIf(edge -> edge.getEdgeKey().equals(change.id()));
      }
    }

    return canvasMapper.toResponse(canvasRepository.saveAndFlush(canvas));
  }

  private CanvasEntity getCanvasForCurrentUser(UUID canvasId) {
    UUID userId = currentUserProvider.currentUserId();
    return canvasRepository
        .findByIdAndUser_Id(canvasId, userId)
        .orElseThrow(
            () ->
                new ApiException(
                    HttpStatus.NOT_FOUND, "canvas_not_found", "Canvas not found for current user."));
  }

  private UserEntity resolveCurrentUser() {
    UUID userId = currentUserProvider.currentUserId();
    return userRepository
        .findById(userId)
        .orElseGet(
            () -> {
              UserEntity user = new UserEntity();
              user.setId(userId);
              user.setEmail("user-" + userId.toString().substring(0, 8) + "@local");
              user.setPasswordHash(null);
              return userRepository.save(user);
            });
  }

  private CanvasNodeEntity createNode(
      String nodeKey, String type, String title, String subtitle, double x, double y) {
    CanvasNodeEntity node = new CanvasNodeEntity();
    node.setNodeKey(nodeKey);
    node.setType(type);
    node.setTitle(title);
    node.setSubtitle(subtitle);
    node.setPositionX(x);
    node.setPositionY(y);
    return node;
  }

  private CanvasEdgeEntity createEdge(String edgeKey, String source, String target, boolean animated) {
    CanvasEdgeEntity edge = new CanvasEdgeEntity();
    edge.setEdgeKey(edgeKey);
    edge.setSourceKey(source);
    edge.setTargetKey(target);
    edge.setAnimated(animated);
    return edge;
  }

  private String normalizeName(String candidate, String fallback) {
    if (candidate == null) {
      return fallback;
    }
    String trimmed = candidate.trim();
    return trimmed.isEmpty() ? fallback : trimmed;
  }

  private double calculateGridX(int nodeIndex) {
    int relative = nodeIndex - 1;
    return GRID_START_X + (relative % GRID_COLUMNS) * GRID_STEP_X;
  }

  private double calculateGridY(int nodeIndex) {
    int relative = nodeIndex - 1;
    return GRID_START_Y + Math.floor(relative / (double) GRID_COLUMNS) * GRID_STEP_Y;
  }
}
