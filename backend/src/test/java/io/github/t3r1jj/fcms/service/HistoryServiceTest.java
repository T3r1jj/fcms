package io.github.t3r1jj.fcms.service;

import io.github.t3r1jj.fcms.model.Event;
import io.github.t3r1jj.fcms.repository.InMemoryEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class HistoryServiceTest {

    private InMemoryEventRepository repository;
    private HistoryService service;

    @BeforeMethod
    public void setUp() {
        repository = new InMemoryEventRepository();
        service = new HistoryService(repository);
    }

    @Test
    public void getAllShouldReturn2EventsAfterAddingThemToRepo() {
        Event event1 = new Event("a", "b", Event.EventType.INFO);
        Event event2 = new Event("aaa", "bbb", Event.EventType.WARNING);
        repository.add(event1);
        repository.add(event2);
        List<Event> history = service.getAll();
        assertTrue(history.contains(event1));
        assertTrue(history.contains(event2));
        assertEquals(2, history.size());
    }

    @Test
    public void getAllLimitTo1() {
        Event event1 = new Event("a", "b", Event.EventType.INFO);
        Event event2 = new Event("aaa", "bbb", Event.EventType.WARNING);
        Event event3 = new Event("cccccc", "cccccc", Event.EventType.ERROR);
        repository.add(event1);
        repository.add(event2);
        repository.add(event3);

        Pageable pageRequest = PageRequest.of(0, 1);
        Page<Event> results = service.getAll(pageRequest);
        assertEquals(3, results.getTotalPages());
        assertEquals(3, results.getTotalElements());
        assertEquals(0, results.getNumber());
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.getContent().contains(event1));
    }

    @Test
    public void getAllOffsetBy1() {
        Event event1 = new Event("a", "b", Event.EventType.INFO);
        Event event2 = new Event("aaa", "bbb", Event.EventType.WARNING);
        Event event3 = new Event("cccccc", "cccccc", Event.EventType.ERROR);
        repository.add(event1);
        repository.add(event2);
        repository.add(event3);

        Pageable pageRequest = PageRequest.of(1, 1);
        Page<Event> results = service.getAll(pageRequest);
        assertEquals(3, results.getTotalPages());
        assertEquals(3, results.getTotalElements());
        assertEquals(1, results.getNumber());
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.getContent().contains(event2));
    }

    @Test
    public void getAllOffsetBy2Uneven() {
        Event event1 = new Event("a", "b", Event.EventType.INFO);
        Event event2 = new Event("aaa", "bbb", Event.EventType.WARNING);
        Event event3 = new Event("cccccc", "cccccc", Event.EventType.ERROR);
        repository.add(event1);
        repository.add(event2);
        repository.add(event3);

        Pageable pageRequest = PageRequest.of(1, 2);
        Page<Event> results = service.getAll(pageRequest);
        assertEquals(2, results.getTotalPages());
        assertEquals(3, results.getTotalElements());
        assertEquals(1, results.getNumber());
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.getContent().contains(event3));
    }

    @Test
    public void addShouldAddEventToRepo() {
        Event event1 = new Event("a", "b", Event.EventType.INFO);
        service.add(event1);
        assertTrue(repository.getAll().contains(event1));
    }

    @Test
    public void deleteAllShouldClearRepo() {
        Event event1 = new Event("a", "b", Event.EventType.INFO);
        Event event2 = new Event("aaa", "bbb", Event.EventType.WARNING);
        repository.add(event1);
        repository.add(event2);
        service.deleteAll();
        assertTrue(repository.getAll().isEmpty());
    }
}