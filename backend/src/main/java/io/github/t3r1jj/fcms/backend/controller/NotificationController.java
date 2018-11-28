package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.service.NotificationService;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.*;
import org.atmosphere.interceptor.CorsInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@ManagedService(path = "/api/notification", interceptors = {NotificationController.MyCorsInterceptor.class})
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

    public static class MyCorsInterceptor extends CorsInterceptor {

        @Override
        public Action inspect(AtmosphereResource r) {
            Action action = super.inspect(r);
            System.out.println("Im here dude " + r.transport() + " " + action);
            if ("OPTIONS".equals(r.getRequest().getMethod())) {
                System.out.println("OPTS ");
                AtmosphereResponse res = r.getResponse();
                res.setHeader("Access-Control-Allow-Headers", res.getHeader("Access-Control-Allow-Headers") + ", Authorization");
                res.setHeader("Access-Control-Allow-Origin", "*");
                res.setHeader("Access-Control-Allow-Credentials", "false");
            } else {
//                final String authorization = r.getRequest().getHeader("Authorization");
//                if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
//                    // Authorization: Basic base64credentials
//                    String base64Credentials = authorization.substring("Basic".length()).trim();
//                    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
//                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
//                    // credentials = username:password
//                    final String[] values = credentials.split(":", 2);
//                    System.out.println(Arrays.toString(values));
//                    if (values.length != 2 || values[0].equals("admin") || values[1].equals("admin")) {
//                        System.out.println("OK");
//                        return action;
//                    }
//                }
//                throw new AccessDeniedException("Not authorized");
            }
            System.out.println(r.getRequest());
            System.out.println(r.getResponse().headers().toString());
            return action;
        }
    }
}