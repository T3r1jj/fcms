package io.github.t3r1jj.fcms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.t3r1jj.fcms.model.Event;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.cpr.AtmosphereResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    public final List<AtmosphereResource> resources = new ArrayList<>();
    private final JacksonEncoder jacksonEncoder = new JacksonEncoder();

    public void broadcast(Event event) {
        String json = jacksonEncoder.encode(event);
        resources.stream()
                .flatMap(r -> r.broadcasters().stream())
                .forEach(b -> b.broadcast(json));
    }

    public static class JacksonEncoder implements Encoder<Event, String> {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String encode(Event e) {
            try {
                return this.mapper.writeValueAsString(e);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }
}
