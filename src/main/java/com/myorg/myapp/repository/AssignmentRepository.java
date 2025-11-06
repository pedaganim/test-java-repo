package com.myorg.myapp.repository;

import com.myorg.myapp.model.Assignment;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
  @Query("select a from Assignment a where a.runway.id = :runwayId and a.startTime < :end and a.endTime > :start order by a.startTime asc")
  List<Assignment> findOverlaps(@Param("runwayId") Long runwayId,
                                @Param("start") Instant start,
                                @Param("end") Instant end);

  @Query("select a from Assignment a where (:runwayId is null or a.runway.id = :runwayId) and a.startTime >= :from and a.endTime <= :to order by a.startTime asc")
  List<Assignment> findAssignments(@Param("from") Instant from,
                                   @Param("to") Instant to,
                                   @Param("runwayId") Long runwayId);
}
