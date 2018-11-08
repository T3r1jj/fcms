package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.service.ConfigurationService;
import io.github.t3r1jj.fcms.backend.model.Configuration;
import io.github.t3r1jj.fcms.backend.model.ExternalService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.when;

public class ConfigurationControllerTest {

    @Mock
    private ConfigurationService configurationService;
    private Configuration defaultConfig;

    @BeforeMethod
    public void setUp() {
        defaultConfig = new Configuration(new ExternalService[]{new ExternalService("Mocked service name", true, true,
                new ExternalService.ApiKey("label123", "key123"))});
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetConfigurationShouldReturnMockedConfiguration() {
        when(configurationService.getConfiguration()).thenReturn(defaultConfig);

        RestAssuredMockMvc.given()
                .standaloneSetup(new ConfigurationController(configurationService))
                .when()
                .get("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("services[0]", hasKey("name"))
                .body("services[0]", hasKey("primary"));
    }

    @Test
    public void testPostConfigurationShouldBe200() {
        RestAssuredMockMvc.given()
                .standaloneSetup(new ConfigurationController(configurationService))
                .contentType(ContentType.JSON)
                .body(defaultConfig)
                .when()
                .post("/api/configuration")
                .then()
                .assertThat()
                .statusCode(200);
    }

}