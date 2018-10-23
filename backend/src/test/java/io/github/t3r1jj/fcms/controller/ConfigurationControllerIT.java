package io.github.t3r1jj.fcms.controller;

import io.github.t3r1jj.fcms.model.Configuration;
import io.github.t3r1jj.fcms.model.ExternalService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigurationControllerIT extends AbstractTestNGSpringContextTests {
    @LocalServerPort
    private int port;

    private Configuration defaultConfig;

    @BeforeTest
    public void setUp() {
        defaultConfig = new Configuration(new ExternalService[]{new ExternalService("Mocked service name", true)});
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetConfigurationShouldReturnMockedConfiguration() {
        RestAssured.given()
                .port(port)
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
                .port(port)
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
                .port(port)
                .contentType(ContentType.JSON)
                .body(defaultConfig)
                .when()
                .post("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200);

        RestAssured.given()
                .port(port)
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