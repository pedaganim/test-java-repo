package com.myorg.myapp.repository;

import com.myorg.myapp.model.Airline;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
  Optional<Airline> findByCode(String code);
}
