package com.myorg.myapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "flight_requests")
public class FlightRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private Airline airline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PriorityTier priority;

  @NotNull
  @Column(nullable = false)
  private Instant earliestTime;

  @Column(nullable = false)
  private int serviceSeconds; // occupancy duration on runway

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RequestStatus status = RequestStatus.PENDING;

  public Long getId() { return id; }
  public Airline getAirline() { return airline; }
  public PriorityTier getPriority() { return priority; }
  public Instant getEarliestTime() { return earliestTime; }
  public int getServiceSeconds() { return serviceSeconds; }
  public RequestStatus getStatus() { return status; }

  public void setId(Long id) { this.id = id; }
  public void setAirline(Airline airline) { this.airline = airline; }
  public void setPriority(PriorityTier priority) { this.priority = priority; }
  public void setEarliestTime(Instant earliestTime) { this.earliestTime = earliestTime; }
  public void setServiceSeconds(int serviceSeconds) { this.serviceSeconds = serviceSeconds; }
  public void setStatus(RequestStatus status) { this.status = status; }
}
