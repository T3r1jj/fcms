package io.github.t3r1jj.fcms.backend.controller;

/*
 * Copyright 2008-2018-2008-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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