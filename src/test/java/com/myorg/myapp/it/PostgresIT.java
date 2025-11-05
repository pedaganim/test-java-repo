package com.myorg.myapp.it;

import java.util.UUID;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class PostgresIT {
  static PostgreSQLContainer<?> pg;
  static boolean usePg = false;
  static final String H2_DB = "itdb_" + UUID.randomUUID();

  static {
    try {
      pg =
          new PostgreSQLContainer<>("postgres:16-alpine")
              .withDatabaseName("app")
              .withUsername("app")
              .withPassword("app");
      pg.start();
      usePg = true;
    } catch (Throwable t) {
      usePg = false; // fallback to H2
    }
  }

  @DynamicPropertySource
  static void dbProps(DynamicPropertyRegistry r) {
    if (usePg) {
      r.add("spring.datasource.url", pg::getJdbcUrl);
      r.add("spring.datasource.username", pg::getUsername);
      r.add("spring.datasource.password", pg::getPassword);
      r.add("spring.datasource.driverClassName", () -> "org.postgresql.Driver");
      r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    } else {
      r.add(
          "spring.datasource.url",
          () -> "jdbc:h2:mem:" + H2_DB + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
      r.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
      r.add("spring.datasource.username", () -> "sa");
      r.add("spring.datasource.password", () -> "");
      r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
    }
    r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    r.add("spring.flyway.enabled", () -> "false");
  }
}
