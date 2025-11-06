package com.myorg.myapp.service;

import com.myorg.myapp.model.Airline;
import com.myorg.myapp.model.Assignment;
import com.myorg.myapp.model.FlightRequest;
import com.myorg.myapp.model.PriorityTier;
import com.myorg.myapp.model.RequestStatus;
import com.myorg.myapp.model.Runway;
import com.myorg.myapp.repository.AirlineRepository;
import com.myorg.myapp.repository.AssignmentRepository;
import com.myorg.myapp.repository.FlightRequestRepository;
import com.myorg.myapp.repository.RunwayRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class SchedulerService {
  private static final Duration SEPARATION_BUFFER = Duration.ofSeconds(60);

  private final FlightRequestRepository requestRepo;
  private final AssignmentRepository assignmentRepo;
  private final RunwayRepository runwayRepo;
  private final AirlineRepository airlineRepo;

  public SchedulerService(
      FlightRequestRepository requestRepo,
      AssignmentRepository assignmentRepo,
      RunwayRepository runwayRepo,
      AirlineRepository airlineRepo) {
    this.requestRepo = requestRepo;
    this.assignmentRepo = assignmentRepo;
    this.runwayRepo = runwayRepo;
    this.airlineRepo = airlineRepo;
  }

  public FlightRequest submitRequest(String airlineCode, PriorityTier priority, Instant earliestTime, int serviceSeconds) {
    Airline airline = airlineRepo.findByCode(airlineCode)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Airline not found"));

    FlightRequest fr = new FlightRequest();
    fr.setAirline(airline);
    fr.setPriority(priority);
    fr.setEarliestTime(earliestTime);
    fr.setServiceSeconds(serviceSeconds);
    fr.setStatus(RequestStatus.PENDING);
    return requestRepo.save(fr);
  }

  public List<Assignment> scheduleWindow(Instant from, Instant to) {
    List<Runway> runways = runwayRepo.findAll();
    if (runways.isEmpty()) {
      throw new ResponseStatusException(BAD_REQUEST, "No runways configured");
    }

    List<FlightRequest> candidates = requestRepo.findEligiblePending(to);
    if (candidates.isEmpty()) {
      return List.of();
    }

    // Group by priority tier then airline for round-robin
    Map<PriorityTier, Map<Long, Queue<FlightRequest>>> queues = candidates.stream()
        .sorted(Comparator.comparing(FlightRequest::getEarliestTime))
        .collect(Collectors.groupingBy(
            FlightRequest::getPriority,
            () -> new EnumMap<>(PriorityTier.class),
            Collectors.groupingBy(fr -> fr.getAirline().getId(), Collectors.toCollection(ArrayDeque::new))
        ));

    List<Assignment> created = new ArrayList<>();

    for (PriorityTier tier : List.of(PriorityTier.EMERGENCY, PriorityTier.VIP, PriorityTier.SCHEDULED, PriorityTier.FERRY)) {
      Map<Long, Queue<FlightRequest>> byAirline = queues.getOrDefault(tier, Map.of());
      if (byAirline.isEmpty()) {
        continue;
      }

      // Simple round-robin across airlines within this tier
      List<Long> airlineIds = new ArrayList<>(byAirline.keySet());
      int nextIdx = 0;
      do {
        if (airlineIds.isEmpty()) {
          break;
        }
        Long airlineId = airlineIds.get(nextIdx % airlineIds.size());
        Queue<FlightRequest> q = byAirline.get(airlineId);
        if (q != null) {
          FlightRequest fr = q.peek();
          if (fr != null) {
            Optional<Assignment> placed = tryPlace(fr, runways, from, to);
            if (placed.isPresent()) {
              // consume and mark
              q.poll();
              fr.setStatus(RequestStatus.ASSIGNED);
              requestRepo.save(fr);
              created.add(assignmentRepo.save(placed.get()));
              if (q.isEmpty()) {
                byAirline.remove(airlineId);
                airlineIds.remove(airlineId);
              }
            } else {
              // could not place now; skip to next airline to maintain fairness
              // if earliest beyond window, drop from this cycle
              if (fr.getEarliestTime().isAfter(to)) {
                q.poll();
              }
            }
          } else {
            byAirline.remove(airlineId);
            airlineIds.remove(airlineId);
          }
        }
        nextIdx++;
      } while (!byAirline.isEmpty());
    }

    return created;
  }

  private Optional<Assignment> tryPlace(FlightRequest fr, List<Runway> runways, Instant windowStart, Instant windowEnd) {
    Instant startCandidate = fr.getEarliestTime().isBefore(windowStart) ? windowStart : fr.getEarliestTime();
    Duration service = Duration.ofSeconds(fr.getServiceSeconds());

    for (Runway rw : runways) {
      Instant t = startCandidate;
      while (!t.isAfter(windowEnd.minus(service))) {
        Instant end = t.plus(service);
        // add separation buffer around slot when checking overlaps
        Instant checkStart = t.minus(SEPARATION_BUFFER);
        Instant checkEnd = end.plus(SEPARATION_BUFFER);
        List<Assignment> overlaps = assignmentRepo.findOverlaps(rw.getId(), checkStart, checkEnd);
        if (overlaps.isEmpty()) {
          Assignment a = new Assignment();
          a.setRequest(fr);
          a.setRunway(rw);
          a.setStartTime(t);
          a.setEndTime(end);
          return Optional.of(a);
        }
        // advance to just after the last overlapping end
        Instant nextT = overlaps.stream().map(Assignment::getEndTime).max(Instant::compareTo).orElse(end);
        t = nextT.plus(SEPARATION_BUFFER);
      }
    }
    return Optional.empty();
  }

  @Transactional(readOnly = true)
  public List<Assignment> listAssignments(Instant from, Instant to, Long runwayId) {
    return assignmentRepo.findAssignments(from, to, runwayId);
  }
}
