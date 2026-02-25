package com.idkwhyyyy.backend.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "canvases")
@Getter
@Setter
@NoArgsConstructor
public class CanvasEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "next_node_index", nullable = false)
  private Integer nextNodeIndex;

  @Version
  @Column(nullable = false)
  private Long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "canvas", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<CanvasNodeEntity> nodes = new ArrayList<>();

  @OneToMany(mappedBy = "canvas", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<CanvasEdgeEntity> edges = new ArrayList<>();

  public void addNode(CanvasNodeEntity node) {
    node.setCanvas(this);
    this.nodes.add(node);
  }

  public void addEdge(CanvasEdgeEntity edge) {
    edge.setCanvas(this);
    this.edges.add(edge);
  }

  @PrePersist
  void onCreate() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
  }
}
