package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigurationControllerIT extends AbstractTestNGSpringContextTests {
    @LocalServerPort
    private int port;

    private Configuration defaultConfig;

    @BeforeMethod
    public void setUp() {
        defaultConfig = new Configuration(new ExternalService[]{new ExternalService("Mocked service name", true)});
        RestAssured.port = port;
    }

    @Test
    public void testGetConfigurationShouldReturnMockedConfiguration() {
        RestAssured.given()
                .when()
                .get("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("apiKeys[0]", hasKey("name"))
                .body("apiKeys[0]", hasKey("primary"));
    }

    @Test
    public void testPostConfigurationShouldBe200() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(defaultConfig)
                .when()
                .post("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void testPostConfigurationShouldUpdateGetConfiguration() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(defaultConfig)
                .when()
                .post("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200);

        RestAssured.given()
                .when()
                .get("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("apiKeys[0].name", equalTo(defaultConfig.getApiKeys()[0].getName()))
                .body("apiKeys[0].primary", equalTo(defaultConfig.getApiKeys()[0].isPrimary()));
    }

}