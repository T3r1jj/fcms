package io.github.t3r1jj.fcms.service;

import io.github.t3r1jj.fcms.model.Event;
import io.github.t3r1jj.fcms.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Autowired
    public HistoryService(EventRepository eventRepository, NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
    }

    public List<Event> getAll() {
        return eventRepository.getAll();
    }

    public Page<Event> getAll(Pageable pageable) {
        return eventRepository.getAll(pageable);
    }

    /**
     * @param event to add to the history and broadcast to listeners
     */
    public void add(Event event) {
        eventRepository.add(event);
        notificationService.broadcast(event);
    }

    public void deleteAll() {
        eventRepository.deleteAll();
    }
}
