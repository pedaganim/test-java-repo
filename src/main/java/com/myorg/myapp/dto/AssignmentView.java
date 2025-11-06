package com.myorg.myapp.dto;

import com.myorg.myapp.model.Assignment;
import java.time.Instant;

public class AssignmentView {
  private Long id;
  private Long requestId;
  private Long runwayId;
  private String runwayCode;
  private String airlineCode;
  private Instant startTime;
  private Instant endTime;

  public static AssignmentView from(Assignment a) {
    AssignmentView v = new AssignmentView();
    v.id = a.getId();
    v.requestId = a.getRequest().getId();
    v.runwayId = a.getRunway().getId();
    v.runwayCode = a.getRunway().getCode();
    v.airlineCode = a.getRequest().getAirline().getCode();
    v.startTime = a.getStartTime();
    v.endTime = a.getEndTime();
    return v;
  }

  public Long getId() { return id; }
  public Long getRequestId() { return requestId; }
  public Long getRunwayId() { return runwayId; }
  public String getRunwayCode() { return runwayCode; }
  public String getAirlineCode() { return airlineCode; }
  public Instant getStartTime() { return startTime; }
  public Instant getEndTime() { return endTime; }
}
