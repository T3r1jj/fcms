package io.github.t3r1jj.fcms.repository;

import io.github.t3r1jj.fcms.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class InMemoryEventRepository implements EventRepository {
    private List<Event> data = new ArrayList<>();

    @Override
    public List<Event> getAll() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public Page<Event> getAll(Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > data.size() ? data.size() : (start + pageable.getPageSize());
        return new PageImpl<>(data.subList(start, end), pageable, data.size());
    }

    @Override
    public void add(Event event) {
        data.add(event);
    }

    @Override
    public void deleteAll() {
        data.clear();
    }
}
