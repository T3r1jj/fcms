package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.service.NotificationService;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@ManagedService(path = "/api/notification")
public class NotificationController extends SpringBeanAutowiringSupport {

    private final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    NotificationService notificationService;
    @Autowired
    BroadcasterFactory factory;

    @Ready
    public void onReady(final AtmosphereResource resource) {
        notificationService.resources.add(resource);
        this.logger.info("Connected {}", resource.uuid());
    }

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        this.logger.info("Client {} disconnected [{}]", event.getResource().uuid(),
                (event.isCancelled() ? "cancelled" : "closed"));
    }
}