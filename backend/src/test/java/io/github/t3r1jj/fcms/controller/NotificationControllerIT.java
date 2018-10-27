package io.github.t3r1jj.fcms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.t3r1jj.fcms.FcmsApplication;
import io.github.t3r1jj.fcms.WebSocketClientConfiguration;
import io.github.t3r1jj.fcms.model.Event;
import io.github.t3r1jj.fcms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FcmsApplication.class)
public class NotificationControllerIT extends AbstractTestNGSpringContextTests {
    @LocalServerPort
    private int port;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ObjectMapper objectMapper;

    private Event event;
    private String eventJson;
    private Thread broadcastingThread;

    @BeforeMethod
    public void setUp() throws JsonProcessingException {
        event = new Event("evenTitle", "eventDescription", Event.EventType.ERROR);
        broadcastingThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(2000);
                    notificationService.broadcast(event);
                }
            } catch (InterruptedException ignored) {
            }
        });
        eventJson = objectMapper.writeValueAsString(event);
    }

    @Test
    public void notificationEndpoint() {
        broadcastingThread.start();
        ConfigurableApplicationContext context = new SpringApplicationBuilder(
                WebSocketClientConfiguration.class, PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + this.port
                        + "/api/notification/websocket")
                .run("--spring.main.web-application-type=none");

        long count = context.getBean(WebSocketClientConfiguration.class).latch.getCount();
        AtomicReference<String> messagePayloadReference = context
                .getBean(WebSocketClientConfiguration.class).messagePayload;
        broadcastingThread.interrupt();
        context.close();
        assertEquals(count, 0L);
        assertEquals(messagePayloadReference.get(), eventJson);
    }

}