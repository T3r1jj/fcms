package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.schedule.ReplicationScheduler;
import io.github.t3r1jj.fcms.backend.service.HistoryService;
import io.github.t3r1jj.fcms.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/replication")
public class ReplicationController {
    private final HistoryService historyService;
    private final ApplicationContext context;
    private final DefaultListableBeanFactory beanFactory;

    public ReplicationController(HistoryService historyService, ApplicationContext context, DefaultListableBeanFactory beanFactory) {
        this.historyService = historyService;
        this.context = context;
        this.beanFactory = beanFactory;
    }

    @PostMapping
    public void restartReplication() {
        historyService.addAndNotify(new Event.Builder()
                .formatTitle("REPLICATION RESTART")
                .formatDescription("Stopping replication process if there is any and starting new one")
                .setType(Event.Type.INFO)
                .build()
        );

        ScheduledAnnotationBeanPostProcessor processor = context.getBean(ScheduledAnnotationBeanPostProcessor.class);
        ReplicationScheduler schedulerBean = context.getBean(ReplicationScheduler.class);
        String beanName = Character.toLowerCase(ReplicationScheduler.class.getSimpleName().charAt(0)) + ReplicationScheduler.class.getSimpleName().substring(1);
        processor.postProcessBeforeDestruction(schedulerBean, beanName);
        processor.postProcessAfterInitialization(schedulerBean, beanName);
    }
}
