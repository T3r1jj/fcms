package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.service.RecordService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RecordControllerTest {

    @Mock
    private RecordService recordService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void uploadValidFileShouldResultIn200() {
        RestAssuredMockMvc
                .given()
                .param("name", "filename")
                .param("tag", "tag")
                .multiPart("file", "some text")
                .standaloneSetup(new RecordController(recordService))
                .when()
                .post("/api/records")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void uploadEmptyFileShouldResultIn422() {
        RestAssuredMockMvc
                .given()
                .param("name", "filename")
                .param("tag", "tag")
                .multiPart("file", "")
                .standaloneSetup(new RecordController(recordService))
                .when()
                .post("/api/records")
                .then()
                .assertThat()
                .statusCode(422);
    }

    @Test
    public void getStoredRecordsShouldReturn200AndData() {
        when(recordService.findAll()).thenReturn(Arrays.asList(
                new StoredRecord("name1", "tag1"),
                new StoredRecord("name1", "tag2"))
        );
        RestAssuredMockMvc
                .given()
                .standaloneSetup(new RecordController(recordService))
                .when()
                .get("/api/records")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2));
    }

    @Test
    public void deleteShouldCallServiceToDeleteAndReturn200() {
        String id = "abc";
        RestAssuredMockMvc
                .given()
                .param("id", id)
                .standaloneSetup(new RecordController(recordService))
                .when()
                .delete("/api/records")
                .then()
                .assertThat()
                .statusCode(200);
        verify(recordService, times(1)).delete(id);
    }
}