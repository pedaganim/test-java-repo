package com.myorg.myapp.controller;

import com.myorg.myapp.dto.AssignmentView;
import com.myorg.myapp.dto.SubmitRequestDto;
import com.myorg.myapp.model.Assignment;
import com.myorg.myapp.model.FlightRequest;
import com.myorg.myapp.service.SchedulerService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/takeoffs", produces = MediaType.APPLICATION_JSON_VALUE)
public class TakeoffController {
  private final SchedulerService schedulerService;

  public TakeoffController(SchedulerService schedulerService) {
    this.schedulerService = schedulerService;
  }

  @PostMapping(path = "/requests", consumes = MediaType.APPLICATION_JSON_VALUE)
  public FlightRequest submit(@Valid @RequestBody SubmitRequestDto dto) {
    return schedulerService.submitRequest(
        dto.getAirlineCode(), dto.getPriority(), dto.getEarliestTime(), dto.getServiceSeconds());
  }

  @PostMapping(path = "/schedule/run")
  public List<AssignmentView> runSchedule(
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    List<Assignment> created = schedulerService.scheduleWindow(from, to);
    return created.stream().map(AssignmentView::from).collect(Collectors.toList());
  }

  @GetMapping(path = "/assignments")
  public List<AssignmentView> listAssignments(
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(value = "runwayId", required = false) Long runwayId) {
    return schedulerService.listAssignments(from, to, runwayId).stream()
        .map(AssignmentView::from)
        .collect(Collectors.toList());
  }
}
