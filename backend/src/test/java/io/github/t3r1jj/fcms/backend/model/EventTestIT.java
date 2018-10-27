package io.github.t3r1jj.fcms.backend.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.testng.Assert.assertTrue;

@SpringBootTest
public class EventTestIT extends AbstractTestNGSpringContextTests {
    private static Log logger = LogFactory.getLog(EventTestIT.class);
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getTimeShouldReturnFormattedJson() throws JsonProcessingException {

        Event event = new Event("ignore", "ignore", Event.EventType.INFO, Instant.ofEpochMilli(1540486296971L));
        String formattedTime = "2018-10-25T16:51:36.971Z";
        String json = objectMapper.writeValueAsString(event);
        logger.info("Event parsed to json: " + json);
        assertTrue(json.contains(formattedTime));
    }
}