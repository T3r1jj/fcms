package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.service.NotificationService;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.interceptor.CorsInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@ManagedService(path = "/api/notification", interceptors = {NotificationController.MyCorsInterceptor.class})
public class NotificationController extends SpringBeanAutowiringSupport {

    private final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Ready
    public void onReady(final AtmosphereResource resource) {
        this.logger.info("Connected {}", resource.uuid());
        if (!resource.getRequest().isDestroyable()) {
            this.logger.info("Added to resources {}", resource.uuid());
            notificationService.add(resource);
        }
    }

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        notificationService.cleanUp(event);
        this.logger.info("Client {} disconnected [{}]", event.getResource().uuid(),
                (event.isCancelled() ? "cancelled" : "closed"));
    }

    public static class MyCorsInterceptor extends CorsInterceptor {

        @Override
        public Action inspect(AtmosphereResource r) {
            Action action = super.inspect(r);
            if ("OPTIONS".equals(r.getRequest().getMethod())) {
                AtmosphereResponse res = r.getResponse();
                res.setHeader("Access-Control-Allow-Headers", res.getHeader("Access-Control-Allow-Headers") + ", Authorization");
                res.setHeader("Access-Control-Allow-Origin", "*");
                res.setHeader("Access-Control-Allow-Credentials", "false");
            }
            return action;
        }
    }
}