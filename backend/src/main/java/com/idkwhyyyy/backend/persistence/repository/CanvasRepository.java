package com.idkwhyyyy.backend.persistence.repository;

import com.idkwhyyyy.backend.persistence.entity.CanvasEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanvasRepository extends JpaRepository<CanvasEntity, UUID> {

  Optional<CanvasEntity> findByIdAndUser_Id(UUID id, UUID userId);

  List<CanvasEntity> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId);

  long countByUser_Id(UUID userId);
}
