package io.github.t3r1jj.fcms.backend.service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.t3r1jj.fcms.backend.model.Event;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class NotificationServiceTest {

    @Test
    public void testCleanUpNotCancelled() {
        NotificationService notificationService = new NotificationService(new ObjectMapper());
        AtmosphereResource resource = mock(AtmosphereResource.class);
        AtmosphereRequest request = mock(AtmosphereRequest.class);
        AtmosphereResourceEvent event = mock(AtmosphereResourceEvent.class);
        when(resource.isCancelled()).thenReturn(false);
        when(resource.getRequest()).thenReturn(request);
        when(resource.uuid()).thenReturn("a");
        when(event.getResource()).thenReturn(resource);
        when(resource.broadcasters()).thenReturn(Collections.emptyList());

        notificationService.add(resource);
        notificationService.cleanUp(event);
        notificationService.broadcast(new Event.Builder().build());

        verify(resource, times(1)).broadcasters();
    }

    @Test
    public void testCleanUpCancelled() {
        NotificationService notificationService = new NotificationService(new ObjectMapper());
        AtmosphereResource resource = mock(AtmosphereResource.class);
        AtmosphereRequest request = mock(AtmosphereRequest.class);
        AtmosphereResourceEvent event = mock(AtmosphereResourceEvent.class);
        when(resource.isCancelled()).thenReturn(true);
        when(resource.getRequest()).thenReturn(request);
        when(resource.uuid()).thenReturn("a");
        when(event.getResource()).thenReturn(resource);
        when(resource.broadcasters()).thenReturn(Collections.emptyList());

        notificationService.add(resource);
        notificationService.cleanUp(event);
        notificationService.broadcast(new Event.Builder().build());

        verify(resource, times(0)).broadcasters();
    }

    @Test
    public void testCleanUpCancelledByEvent() {
        NotificationService notificationService = new NotificationService(new ObjectMapper());
        AtmosphereResource resource = mock(AtmosphereResource.class);
        AtmosphereRequest request = mock(AtmosphereRequest.class);
        AtmosphereResourceEvent event = mock(AtmosphereResourceEvent.class);
        when(resource.isCancelled()).thenReturn(false);
        when(resource.getRequest()).thenReturn(request);
        when(resource.uuid()).thenReturn("a");
        when(event.getResource()).thenReturn(resource);
        when(resource.broadcasters()).thenReturn(Collections.emptyList());

        when(event.isCancelled()).thenReturn(true);

        notificationService.add(resource);
        notificationService.cleanUp(event);
        notificationService.broadcast(new Event.Builder().build());

        verify(resource, times(0)).broadcasters();
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void testUnexpectedJsonGenerationException() throws JsonProcessingException {
        ObjectMapper objectMapper = spy(new ObjectMapper());
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonGenerationException("Mocked"));
        Encoder<Event, String> eventEncoder = new NotificationService.JacksonEncoder(objectMapper);
        eventEncoder.encode(new Event.Builder().build());
    }

}