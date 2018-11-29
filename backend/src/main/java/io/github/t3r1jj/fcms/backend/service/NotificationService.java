package io.github.t3r1jj.fcms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.t3r1jj.fcms.backend.model.Event;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This service is only for notifications. Use {@link HistoryService} for logging events together with notification.
 */
@Service
public class NotificationService {
    private final List<AtmosphereResource> resources = new CopyOnWriteArrayList<>();
    private final JacksonEncoder jacksonEncoder;

    @Autowired
    public NotificationService(ObjectMapper objectMapper) {
        this.jacksonEncoder = new JacksonEncoder(objectMapper);
    }

    public void broadcast(Event event) {
        String json = jacksonEncoder.encode(event);
        resources.stream()
                .flatMap(r -> r.broadcasters().stream())
                .forEach(b -> b.broadcast(json));
    }

    public void add(AtmosphereResource resource) {
        resources.add(resource);
    }

    public void cleanUp(AtmosphereResourceEvent event) {
        resources.removeIf(r -> r.isCancelled() || event.isCancelled() && event.getResource().uuid().equals(r.uuid()));
    }

    public static class JacksonEncoder implements Encoder<Event, String> {

        private final ObjectMapper mapper;

        JacksonEncoder(ObjectMapper mapper) {
            this.mapper = mapper;
        }

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
