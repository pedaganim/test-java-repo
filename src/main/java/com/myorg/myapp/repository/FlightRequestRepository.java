package com.myorg.myapp.repository;

import com.myorg.myapp.model.FlightRequest;
import com.myorg.myapp.model.RequestStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlightRequestRepository extends JpaRepository<FlightRequest, Long> {
  List<FlightRequest> findByStatus(RequestStatus status);

  @Query("select fr from FlightRequest fr where fr.status = 'PENDING' and fr.earliestTime <= :horizonEnd")
  List<FlightRequest> findEligiblePending(@Param("horizonEnd") Instant horizonEnd);
}
