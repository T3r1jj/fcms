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

    @Autowired
    public HistoryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAll() {
        return eventRepository.getAll();
    }

    public Page<Event> getAll(Pageable pageable) {
        return eventRepository.getAll(pageable);
    }

    public void add(Event event) {
        eventRepository.add(event);
    }

    public void deleteAll() {
        eventRepository.deleteAll();
    }
}
