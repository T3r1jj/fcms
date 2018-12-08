package io.github.t3r1jj.fcms.backend.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthControllerIT extends AbstractTestNGSpringContextTests {


    @LocalServerPort
    private int port;

    @BeforeMethod
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testGetHealth() {
        RestAssured
                .given()
                .auth().basic("admin", "admin")
                .when()
                .get("/api/history")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("dbLimit", not(isEmptyOrNullString()))
                .body("dbSize", not(isEmptyOrNullString()))
                .body("bandwidth", not(isEmptyOrNullString()))
                .body("storageQuotas", not(isEmptyOrNullString()));
    }
}