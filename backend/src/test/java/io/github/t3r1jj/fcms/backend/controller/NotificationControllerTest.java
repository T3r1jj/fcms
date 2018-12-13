package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.service.NotificationService;
import org.atmosphere.cpr.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;
    private NotificationController notificationController;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        notificationController = new NotificationController(notificationService);
    }

    @Test
    public void testOnReady() {
        AtmosphereResource atmosphereResource = mock(AtmosphereResource.class);
        AtmosphereRequest atmosphereRequest = mock(AtmosphereRequest.class);
        when(atmosphereResource.getRequest()).thenReturn(atmosphereRequest);
        when(atmosphereRequest.isDestroyable()).thenReturn(false);
        notificationController.onReady(atmosphereResource);

        verify(notificationService).add(atmosphereResource);
    }

    @Test
    public void testOnReadyDestroyable() {
        AtmosphereResource atmosphereResource = mock(AtmosphereResource.class);
        AtmosphereRequest atmosphereRequest = mock(AtmosphereRequest.class);
        when(atmosphereResource.getRequest()).thenReturn(atmosphereRequest);
        when(atmosphereRequest.isDestroyable()).thenReturn(true);
        notificationController.onReady(atmosphereResource);

        verify(notificationService, times(0)).add(any());
    }

    @Test
    public void testOnDisconnectCancelled() {
        AtmosphereResource atmosphereResource = mock(AtmosphereResource.class);
        AtmosphereRequest atmosphereRequest = mock(AtmosphereRequest.class);
        AtmosphereResourceEvent resourceEvent = mock(AtmosphereResourceEvent.class);
        when(resourceEvent.isCancelled()).thenReturn(true);
        when(resourceEvent.getResource()).thenReturn(atmosphereResource);
        when(atmosphereResource.getRequest()).thenReturn(atmosphereRequest);
        when(atmosphereRequest.uuid()).thenReturn("some uuid");
        notificationController.onDisconnect(resourceEvent);

        verify(notificationService).cleanUp(resourceEvent);
    }

    @Test
    public void testOnDisconnectClosed() {
        AtmosphereResource atmosphereResource = mock(AtmosphereResource.class);
        AtmosphereRequest atmosphereRequest = mock(AtmosphereRequest.class);
        AtmosphereResourceEvent resourceEvent = mock(AtmosphereResourceEvent.class);
        when(resourceEvent.isCancelled()).thenReturn(false);
        when(resourceEvent.getResource()).thenReturn(atmosphereResource);
        when(atmosphereResource.getRequest()).thenReturn(atmosphereRequest);
        when(atmosphereRequest.uuid()).thenReturn("some uuid");
        notificationController.onDisconnect(resourceEvent);

        verify(notificationService).cleanUp(resourceEvent);
    }

    @Test
    public void testCorsInterceptor() {
        NotificationController.MyCorsInterceptor corsInterceptor = new NotificationController.MyCorsInterceptor();
        AtmosphereResourceImpl resource = spy(new AtmosphereResourceImpl());
        AtmosphereRequest request = spy(AtmosphereRequestImpl.newInstance());
        AtmosphereResponse response = spy(AtmosphereResponseImpl.newInstance());
        doReturn(request).when(resource).getRequest();
        doReturn(request).when(resource).getRequest(false);
        doReturn(response).when(resource).getResponse();
        doReturn(response).when(resource).getResponse(false);
        doReturn(null).when(request).getAttribute(FrameworkConfig.WEBSOCKET_MESSAGE);
        request.method("OPTIONS");
        corsInterceptor.inspect(resource);
        assertTrue(response.getHeader("Access-Control-Allow-Headers").contains("Authorization"), "Request contains authorization header");
        assertNotEquals(response.getHeader("Access-Control-Allow-Headers"), ", Authorization", "Request contains additional headers");
        assertEquals(response.getHeader("Access-Control-Allow-Origin"), "*", "Any origin");
        assertEquals(response.getHeader("Access-Control-Allow-Credentials"), "false", "Don't allow credentials (manual credentials, not supplied by the browser)");
    }
}