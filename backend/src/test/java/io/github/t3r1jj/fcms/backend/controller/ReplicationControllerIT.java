package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.schedule.ReplicationScheduler;
import io.github.t3r1jj.fcms.backend.service.ReplicationService;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@PropertySource("classpath:application_scheduling.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReplicationControllerIT extends AbstractTestNGSpringContextTests {
    @LocalServerPort
    private int port;
    @SpyBean
    @Autowired
    private ReplicationScheduler replicationScheduler;
    @SpyBean
    @Autowired
    private ReplicationService replicationService;

    @BeforeMethod
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testRestartReplication() {
        verify(replicationService, times(1)).safelyReplicateAll();
        RestAssured
                .given()
                .when()
                .post("/api/replication")
                .then()
                .assertThat()
                .statusCode(200);
        verify(replicationService, times(2)).safelyReplicateAll();
    }
}