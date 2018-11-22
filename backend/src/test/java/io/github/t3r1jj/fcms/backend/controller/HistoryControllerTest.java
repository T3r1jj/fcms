package io.github.t3r1jj.fcms.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.service.HistoryService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

public class HistoryControllerTest {

    @Mock
    private HistoryService historyService;
    private List<Event> wholeHistory;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        wholeHistory = Arrays.asList(
                new Event("event1", "description1", Event.EventType.INFO),
                new Event("event22", "description22", Event.EventType.WARNING),
                new Event("event333", "description333", Event.EventType.ERROR));
    }

    @Test
    public void getAllNoParamsShouldReturn200AndCorrectData() throws JsonProcessingException {
        when(historyService.findAll()).thenReturn(wholeHistory);
        RestAssuredMockMvc
                .given()
                .standaloneSetup(new HistoryController(historyService))
                .when()
                .get("/api/history")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(3))
                .body("[0].title", is(wholeHistory.get(0).getTitle()))
                .body("[0].description", is(wholeHistory.get(0).getDescription()))
                .body("[0]", hasKey("time"))
                .body("[1].title", is(wholeHistory.get(1).getTitle()));
    }

    @Test
    public void getAllWithParamsShouldReturn200AndMockedData() {
        int size = 2;
        int page = 0;
        PageImpl<Event> eventsPage = new PageImpl<>(wholeHistory, PageRequest.of(page, size), 5);
        when(historyService.findAll(notNull())).thenReturn(eventsPage);
        RestAssuredMockMvc
                .given()
                .standaloneSetup(new HistoryController(historyService))
                .param("size", size)
                .param("page", page)
                .when()
                .get("/api/history")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("totalPages", is(Math.round(5f / size)))
                .body("totalElements", is(5))
                .body("number", is(page))
                .body("numberOfElements", is(wholeHistory.size()))
                .body("content[0].title", is(wholeHistory.get(0).getTitle()))
                .body("content[0].description", is(wholeHistory.get(0).getDescription()))
                .body("content[0]", hasKey("time"))
                .body("content[1].title", is(wholeHistory.get(1).getTitle()));
    }

    @Test
    public void deleteAllShouldReturnStatus200() {
        RestAssuredMockMvc
                .given()
                .standaloneSetup(new HistoryController(historyService))
                .when()
                .delete("/api/history")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void readAllShouldReturnStatus200() {
        RestAssuredMockMvc
                .given()
                .standaloneSetup(new HistoryController(historyService))
                .when()
                .patch("/api/history")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void readOneShouldReturnStatus200() {
        RestAssuredMockMvc
                .given()
                .standaloneSetup(new HistoryController(historyService))
                .param("eventId","")
                .when()
                .post("/api/history")
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void countAllUnreadShouldReturnCountAnd200Status() {
        RestAssuredMockMvc
                .given()
                .standaloneSetup(new HistoryController(historyService))
                .when()
                .get("/api/history/unread")
                .then()
                .assertThat()
                .statusCode(200)
                .body(is(equalTo("0")));
    }

}

