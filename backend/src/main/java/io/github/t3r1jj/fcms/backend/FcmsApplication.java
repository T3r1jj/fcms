package io.github.t3r1jj.fcms.backend;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.ContainerInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.HashSet;

@SpringBootApplication
@EnableScheduling
public class FcmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FcmsApplication.class, args);
    }

    @Bean
    public EmbeddedAtmosphereInitializer atmosphereInitializer() {
        return new EmbeddedAtmosphereInitializer();
    }

    @Bean
    public ServletRegistrationBean<AtmosphereServlet> atmosphereServlet() {
        AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
        atmosphereServlet.framework().setHandlersPath("/");
        ServletRegistrationBean<AtmosphereServlet> registration = new ServletRegistrationBean<>(
                atmosphereServlet, "/api/notification/*");
        registration.addInitParameter("org.atmosphere.cpr.packages", "io.github.t3r1jj.fcms");
        registration.addInitParameter("org.atmosphere.interceptor.HeartbeatInterceptor"
                + ".clientHeartbeatFrequencyInSeconds", "10");
        registration.addInitParameter("org.atmosphere.cpr.objectFactory", "org.atmosphere.spring.SpringWebObjectFactory");
        registration.setLoadOnStartup(0);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    private static class EmbeddedAtmosphereInitializer extends ContainerInitializer
            implements ServletContextInitializer {

        @Override
        public void onStartup(ServletContext servletContext) throws ServletException {
            onStartup(new HashSet<>(), servletContext);
        }

    }
}
