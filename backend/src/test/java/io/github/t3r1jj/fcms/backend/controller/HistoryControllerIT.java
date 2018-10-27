package io.github.t3r1jj.fcms.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.repository.EventRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HistoryControllerIT extends AbstractTestNGSpringContextTests {
    @LocalServerPort
    private int port;

    private static DateTimeFormatter dateFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .withZone(ZoneId.of("UTC"));

    @Autowired
    private EventRepository eventRepository;
    private List<Event> wholeHistory;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        RestAssured.port = port;
        wholeHistory = Arrays.asList(
                new Event("event1", "description1", Event.EventType.INFO),
                new Event("event22", "description22", Event.EventType.WARNING),
                new Event("event333", "descriptio333", Event.EventType.ERROR));
    }

    @Test
    public void getAllNoParamsShouldReturn200AndCorrectDataWithFormattedDate() throws JsonProcessingException {
        wholeHistory.forEach(eventRepository::add);
        RestAssured
                .given()
                .when()
                .get("/api/history")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(3))
                .body("[0].title", is(wholeHistory.get(0).getTitle()))
                .body("[0].description", is(wholeHistory.get(0).getDescription()))
                .body("[0].time", is(dateFormat.format(wholeHistory.get(0).getTime())))
                .body("[1].title", is(wholeHistory.get(1).getTitle()));
    }

}

