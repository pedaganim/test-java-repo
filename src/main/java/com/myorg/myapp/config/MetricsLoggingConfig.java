package com.myorg.myapp.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
    prefix = "management.metrics.export.logging",
    name = "enabled",
    havingValue = "true")
public class MetricsLoggingConfig {

  @Bean
  public LoggingMeterRegistry loggingMeterRegistry(
      Clock clock, @Value("${management.metrics.export.logging.step:10s}") Duration step) {
    LoggingRegistryConfig config =
        new LoggingRegistryConfig() {
          @Override
          public String get(String k) {
            return null;
          }

          @Override
          public Duration step() {
            return step;
          }
        };
    return new LoggingMeterRegistry(config, clock);
  }
}
