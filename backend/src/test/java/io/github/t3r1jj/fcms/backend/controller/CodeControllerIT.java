package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.model.code.Code;
import io.github.t3r1jj.fcms.backend.model.code.OnReplicationCode;
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
                .param("type", Code.Type.AfterReplicationCode)
                .post("/api/code")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void testCheck422() {
        codeRepository.save(new OnReplicationCode.Builder().setCode("SIODJAS").build());

        RestAssured.given()
                .param("type", Code.Type.OnReplicationCode)
                .post("/api/code")
                .then()
                .assertThat()
                .statusCode(422);
    }

    @Test
    public void testCheck200() {
        codeRepository.save(new OnReplicationCode.Builder().build());

        RestAssured.given()
                .param("type", Code.Type.OnReplicationCode)
                .get("/api/code")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void testUpdate() {
        OnReplicationCode code = new OnReplicationCode.Builder().setCode("test").build();

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(code)
                .patch("/api/code")
                .then()
                .assertThat()
                .statusCode(200);

        assertTrue(codeRepository.findById(Code.Type.OnReplicationCode.getId()).isPresent());
    }

    @Test
    public void testDelete() {
        OnReplicationCode code = new OnReplicationCode.Builder().build();
        codeRepository.save(code);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(code)
                .patch("/api/code")
                .then()
                .assertThat()
                .statusCode(200);

        assertFalse(codeRepository.findById(Code.Type.OnReplicationCode.getId()).isPresent());
    }

}