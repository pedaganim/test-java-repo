package com.myorg.myapp.it;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiIT extends PostgresIT {

  @LocalServerPort int port;

  @BeforeEach
  void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void full_user_flow_create_list_update_conflict_delete() {
    String aliceEmail = "alice-" + UUID.randomUUID() + "@example.com";
    String bobEmail = "bob-" + UUID.randomUUID() + "@example.com";
    // create
    long id =
        createUser("Alice", aliceEmail, "admin")
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Alice"))
            .extract()
            .jsonPath()
            .getLong("id");

    // list contains Alice
    RestAssured.given()
        .when()
        .get("/api/users")
        .then()
        .statusCode(200)
        .body("email", hasItem(aliceEmail));

    // update
    RestAssured.given()
        .contentType(JSON)
        .body(Map.of("name", "Alice Smith", "email", aliceEmail, "role", "user"))
        .when()
        .put("/api/users/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("Alice Smith"))
        .body("role", equalTo("user"));

    // create another user
    long id2 =
        createUser("Bob", bobEmail, "user").statusCode(201).extract().jsonPath().getLong("id");

    // attempt to change Bob's email to Alice's -> 409
    RestAssured.given()
        .contentType(JSON)
        .body(Map.of("name", "Bob", "email", aliceEmail, "role", "user"))
        .when()
        .put("/api/users/" + id2)
        .then()
        .statusCode(409);

    // delete Alice
    RestAssured.given().when().delete("/api/users/" + id).then().statusCode(204);

    // get Alice -> 404
    RestAssured.given().when().get("/api/users/" + id).then().statusCode(404);
  }

  private ValidatableResponse createUser(String name, String email, String role) {
    return RestAssured.given()
        .contentType(JSON)
        .body(Map.of("name", name, "email", email, "role", role))
        .when()
        .post("/api/users")
        .then();
  }
}
