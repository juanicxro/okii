package com.idkwhyyyy.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "canvas_nodes")
@Getter
@Setter
@NoArgsConstructor
public class CanvasNodeEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "canvas_id", nullable = false)
  private CanvasEntity canvas;

  @Column(name = "node_key", nullable = false, length = 80)
  private String nodeKey;

  @Column(nullable = false, length = 40)
  private String type;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false, length = 255)
  private String subtitle;

  @Column(name = "position_x", nullable = false)
  private Double positionX;

  @Column(name = "position_y", nullable = false)
  private Double positionY;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

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
