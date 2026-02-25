package com.idkwhyyyy.backend.canvas.api;

import com.idkwhyyyy.backend.canvas.api.dto.CanvasResponse;
import com.idkwhyyyy.backend.canvas.api.dto.CanvasSummaryResponse;
import com.idkwhyyyy.backend.canvas.api.dto.CreateCanvasRequest;
import com.idkwhyyyy.backend.canvas.api.dto.CreateEdgeRequest;
import com.idkwhyyyy.backend.canvas.api.dto.CreateNodeRequest;
import com.idkwhyyyy.backend.canvas.api.dto.PatchEdgesRequest;
import com.idkwhyyyy.backend.canvas.api.dto.PatchNodesRequest;
import com.idkwhyyyy.backend.canvas.api.dto.RenameCanvasRequest;
import com.idkwhyyyy.backend.canvas.service.CanvasService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/canvases")
public class CanvasController {

  private final CanvasService canvasService;

  public CanvasController(CanvasService canvasService) {
    this.canvasService = canvasService;
  }

  @GetMapping
  public List<CanvasSummaryResponse> listCanvases() {
    return canvasService.listCanvases();
  }

  @GetMapping("/{canvasId}")
  public CanvasResponse getCanvas(@PathVariable UUID canvasId) {
    return canvasService.getCanvas(canvasId);
  }

  @PostMapping
  public ResponseEntity<CanvasResponse> createCanvas(
      @Valid @RequestBody(required = false) CreateCanvasRequest request) {
    CanvasResponse created = canvasService.createCanvas(request);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @PatchMapping("/{canvasId}")
  public CanvasResponse renameCanvas(
      @PathVariable UUID canvasId, @Valid @RequestBody(required = false) RenameCanvasRequest request) {
    return canvasService.renameCanvas(canvasId, request);
  }

  @DeleteMapping("/{canvasId}")
  public ResponseEntity<Void> deleteCanvas(@PathVariable UUID canvasId) {
    canvasService.deleteCanvas(canvasId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{canvasId}/nodes")
  public CanvasResponse createNode(
      @PathVariable UUID canvasId, @Valid @RequestBody(required = false) CreateNodeRequest request) {
    return canvasService.createNode(canvasId, request);
  }

  @PatchMapping("/{canvasId}/nodes")
  public CanvasResponse patchNodes(
      @PathVariable UUID canvasId, @Valid @RequestBody(required = false) PatchNodesRequest request) {
    return canvasService.applyNodeChanges(canvasId, request);
  }

  @PostMapping("/{canvasId}/edges")
  public CanvasResponse createEdge(
      @PathVariable UUID canvasId, @Valid @RequestBody CreateEdgeRequest request) {
    return canvasService.createEdge(canvasId, request);
  }

  @PatchMapping("/{canvasId}/edges")
  public CanvasResponse patchEdges(
      @PathVariable UUID canvasId, @Valid @RequestBody(required = false) PatchEdgesRequest request) {
    return canvasService.applyEdgeChanges(canvasId, request);
  }
}

