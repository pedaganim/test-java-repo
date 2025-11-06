package com.myorg.myapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "assignments")
public class Assignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private FlightRequest request;

  @ManyToOne(optional = false)
  private Runway runway;

  @NotNull
  @Column(nullable = false)
  private Instant startTime;

  @NotNull
  @Column(nullable = false)
  private Instant endTime;

  public Long getId() { return id; }
  public FlightRequest getRequest() { return request; }
  public Runway getRunway() { return runway; }
  public Instant getStartTime() { return startTime; }
  public Instant getEndTime() { return endTime; }

  public void setId(Long id) { this.id = id; }
  public void setRequest(FlightRequest request) { this.request = request; }
  public void setRunway(Runway runway) { this.runway = runway; }
  public void setStartTime(Instant startTime) { this.startTime = startTime; }
  public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
