package com.idkwhyyyy.backend.persistence.repository;

import com.idkwhyyyy.backend.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);
}
