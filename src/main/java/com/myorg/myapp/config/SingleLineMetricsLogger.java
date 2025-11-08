package com.myorg.myapp.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Logs a single consolidated line of key metrics each interval.
 */
@Component
@ConditionalOnProperty(prefix = "app.metrics.singleline", name = "enabled", havingValue = "true")
public class SingleLineMetricsLogger {

  private static final Logger log = LoggerFactory.getLogger(SingleLineMetricsLogger.class);
  private final MeterRegistry registry;
  private final ObjectMapper objectMapper;

  public SingleLineMetricsLogger(MeterRegistry registry, ObjectMapper objectMapper) {
    this.registry = registry;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedRateString = "${app.metrics.singleline.interval-ms:10000}")
  public void logOnce() {
    // Collect a subset of useful metrics. You can extend easily.
    Double procCpu = gauge("process.cpu.usage"); // 0..1
    Double sysCpu = gauge("system.cpu.usage");   // 0..1
    Double heapUsed = gauge("jvm.memory.used", List.of(Tag.of("area", "heap")));
    Double heapCommitted = gauge("jvm.memory.committed", List.of(Tag.of("area", "heap")));
    Double heapMax = gauge("jvm.memory.max", List.of(Tag.of("area", "heap")));

    LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
    payload.put("type", "metrics");
    payload.put("time", Instant.now().toString());
    if (heapUsed != null) payload.put("heap_used", heapUsed.longValue());
    if (heapCommitted != null) payload.put("heap_committed", heapCommitted.longValue());
    if (heapMax != null) payload.put("heap_max", heapMax.longValue());
    if (procCpu != null) payload.put("process_cpu_pct", Math.round(procCpu * 10000.0) / 100.0);
    if (sysCpu != null) payload.put("system_cpu_pct", Math.round(sysCpu * 10000.0) / 100.0);

    try {
      log.info(objectMapper.writeValueAsString(payload));
    } catch (Exception e) {
      // Fallback to plain text if serialization fails
      StringJoiner j = new StringJoiner(" ", "metrics time=" + payload.get("time") + " ", "");
      if (payload.containsKey("heap_used")) j.add("heap_used=" + payload.get("heap_used"));
      if (payload.containsKey("heap_committed")) j.add("heap_committed=" + payload.get("heap_committed"));
      if (payload.containsKey("heap_max")) j.add("heap_max=" + payload.get("heap_max"));
      if (payload.containsKey("process_cpu_pct")) j.add("process_cpu_pct=" + payload.get("process_cpu_pct"));
      if (payload.containsKey("system_cpu_pct")) j.add("system_cpu_pct=" + payload.get("system_cpu_pct"));
      log.info(j.toString());
    }
  }

  private Double gauge(String name) {
    Gauge g = registry.find(name).gauge();
    return g != null ? g.value() : null;
  }

  private Double gauge(String name, List<Tag> tags) {
    Gauge g = registry.find(name).tags(tags).gauge();
    return g != null ? g.value() : null;
  }
}
