package io.github.t3r1jj.fcms.backend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WebSocketClientConfiguration implements CommandLineRunner {
    private static Log logger = LogFactory.getLog(WebSocketClientConfiguration.class);

    @Value("${websocket.uri}")
    private String webSocketUri;

    public final CountDownLatch latch = new CountDownLatch(1);

    public final AtomicReference<String> messagePayload = new AtomicReference<>();

    @Override
    public void run(String... args) throws Exception {
        logger.info("Waiting for response: latch=" + this.latch.getCount());
        if (this.latch.await(10, TimeUnit.SECONDS)) {
            logger.info("Got response: " + this.messagePayload.get());
        } else {
            logger.info("Response not received: latch=" + this.latch.getCount());
        }
    }

    @Bean
    public WebSocketConnectionManager wsConnectionManager() {
        WebSocketConnectionManager manager = new WebSocketConnectionManager(client(),
                handler(), this.webSocketUri);
        String auth = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
        manager.getHeaders().add("Authorization", "Basic " + auth);
        manager.setAutoStartup(true);
        return manager;
    }

    @Bean
    public StandardWebSocketClient client() {
        return new StandardWebSocketClient();
    }

    @Bean
    public TextWebSocketHandler handler() {
        return new TextWebSocketHandler() {

            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                logger.info("Estabilished connection");
            }

            @Override
            protected void handleTextMessage(WebSocketSession session,
                                             TextMessage message) throws Exception {
                logger.info("Received: " + message + " (" + latch.getCount() + ")");
                session.close();
                messagePayload.set(message.getPayload());
                latch.countDown();
            }
        };
    }

}