package io.github.t3r1jj.fcms.controller;

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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FcmsApplication.class)
public class NotificationControllerIT extends AbstractTestNGSpringContextTests {
    @LocalServerPort
    private int port;

    @Autowired
    private NotificationService notificationService;

    private Event event;
    private String json;
    private Thread broadcastingThread;

    @BeforeTest
    public void setUp() {
        event = new Event("evenTitle", "eventDescription", Event.EventType.ERROR);
        json = "{\"title\":\"evenTitle\",\"description\":\"eventDescription\",\"type\":\"ERROR\",\"time\":{\"epochSecond\":"
                + event.getTime().getEpochSecond() + ",\"nano\":" + event.getTime().getNano() + "}}";
        broadcastingThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(2000);
                    notificationService.broadcast(event);
                }
            } catch (InterruptedException ignored) {
            }
        });
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
        assertEquals(messagePayloadReference.get(), json);
    }

}