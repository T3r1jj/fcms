package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.repository.EventRepository;
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

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByTimeDesc();
    }

    public Page<Event> findAll(Pageable pageable) {
        return eventRepository.findAllByOrderByTimeDesc(pageable);
    }

    /**
     * @param event to save to the history and broadcast to listeners
     */
    public void addAndNotify(Event event) {
        eventRepository.save(event);
        notificationService.broadcast(event);
    }

    public void deleteAll() {
        eventRepository.deleteAll();
    }
}
