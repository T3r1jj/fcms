package io.github.t3r1jj.fcms.controller;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.hasKey;

public class ConfigurationControllerTest {

    @Test
    public void testGetConfigurationShouldBeMocked() {
        RestAssuredMockMvc.given()
                .standaloneSetup(new ConfigurationController())
                .when()
                .get("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("apiKeys[0]", hasKey("name"))
                .body("apiKeys[0]", hasKey("primary"));
    }
}