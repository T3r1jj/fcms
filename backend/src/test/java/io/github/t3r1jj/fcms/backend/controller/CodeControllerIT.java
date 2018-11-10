package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.model.code.Code;
import io.github.t3r1jj.fcms.backend.model.code.OnReplicationCallback;
import io.github.t3r1jj.fcms.backend.repository.CodeRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CodeControllerIT extends AbstractTestNGSpringContextTests {

    @LocalServerPort
    private int port;

    @Autowired
    private CodeRepository codeRepository;

    @BeforeMethod
    public void setUp() {
        RestAssured.port = port;
        codeRepository.deleteAll();
    }

    @Test
    public void testCheck404() {
        RestAssured.given()
                .param("type", Code.Type.AfterReplicationCallback)
                .get("/api/code")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void testCheck422() {
        codeRepository.save(new OnReplicationCallback.Builder().setCode("SIODJAS").build());

        RestAssured.given()
                .param("type", Code.Type.OnReplicationCallback)
                .get("/api/code")
                .then()
                .assertThat()
                .statusCode(422);
    }

    @Test
    public void testCheck200() {
        codeRepository.save(new OnReplicationCallback.Builder().build());

        RestAssured.given()
                .param("type", Code.Type.OnReplicationCallback)
                .get("/api/code")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void testUpdate() {
        OnReplicationCallback code = new OnReplicationCallback.Builder().build();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(code)
                .post("/api/code")
                .then()
                .assertThat()
                .statusCode(200);

        assertTrue(codeRepository.findById(Code.Type.OnReplicationCallback.getId()).isPresent());
    }

    @Test
    public void testDelete() {
        codeRepository.save(new OnReplicationCallback.Builder().build());

        RestAssured.given()
                .param("type", Code.Type.OnReplicationCallback)
                .delete("/api/code")
                .then()
                .assertThat()
                .statusCode(200);

        assertFalse(codeRepository.findById(Code.Type.OnReplicationCallback.getId()).isPresent());
    }

}