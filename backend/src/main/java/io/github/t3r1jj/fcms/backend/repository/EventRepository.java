package io.github.t3r1jj.fcms.backend.repository;

import io.github.t3r1jj.fcms.backend.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository {
    List<Event> getAll();

    Page<Event> getAll(Pageable pageable);

    void add(Event event);

    void deleteAll();
}