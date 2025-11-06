package com.myorg.myapp.repository;

import com.myorg.myapp.model.Runway;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunwayRepository extends JpaRepository<Runway, Long> {
  Optional<Runway> findByCode(String code);
}
