package com.myorg.myapp.it;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloApiIT extends PostgresIT {

  @LocalServerPort int port;

  @BeforeEach
  void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void hello_returnsHelloWorld() {
    RestAssured.given()
        .when()
        .get("/api/hello")
        .then()
        .statusCode(200)
        .body(equalTo("Hello, World!"));
  }
}
