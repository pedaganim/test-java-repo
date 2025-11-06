package com.myorg.myapp.dto;

import com.myorg.myapp.model.PriorityTier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class SubmitRequestDto {
  @NotBlank
  private String airlineCode;

  @NotNull
  private PriorityTier priority;

  @NotNull
  private Instant earliestTime;

  @Min(1)
  private int serviceSeconds;

  public String getAirlineCode() { return airlineCode; }
  public void setAirlineCode(String airlineCode) { this.airlineCode = airlineCode; }
  public PriorityTier getPriority() { return priority; }
  public void setPriority(PriorityTier priority) { this.priority = priority; }
  public Instant getEarliestTime() { return earliestTime; }
  public void setEarliestTime(Instant earliestTime) { this.earliestTime = earliestTime; }
  public int getServiceSeconds() { return serviceSeconds; }
  public void setServiceSeconds(int serviceSeconds) { this.serviceSeconds = serviceSeconds; }
}
