package io.github.t3r1jj.fcms.backend.service;

import com.google.common.collect.Lists;
import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.repository.EventRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class HistoryServiceTest {

    private HistoryService service;
    @Mock
    private EventRepository repository;
    @Mock
    private NotificationService notificationService;
    private InMemoryEventRepository inMemoryEventRepository;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        inMemoryEventRepository = new InMemoryEventRepository();
        when(repository.findAll()).thenReturn(inMemoryEventRepository.findAll());
        when(repository.findAllByOrderByTimeDesc()).thenReturn(inMemoryEventRepository.findAllInverted());
        when(repository.findAllByOrderByTimeDesc(any(Pageable.class))).thenAnswer((invocation -> inMemoryEventRepository.findAllInverted(invocation.getArgument(0))));
        when(repository.save(any(Event.class))).thenAnswer((invocation -> inMemoryEventRepository.save(invocation.getArgument(0))));
        doAnswer(invocation -> {
            inMemoryEventRepository.save(invocation.getArgument(0));
            return null;
        }).when(repository).save(any());
        doAnswer(invocation -> {
            inMemoryEventRepository.deleteAll();
            return null;
        }).when(repository).deleteAll();
        service = new HistoryService(repository, notificationService, null);
    }

    @Test
    public void getAllShouldReturn2EventsAfterAddingThemToRepo() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        repository.save(event1);
        repository.save(event2);
        List<Event> history = service.findAll();
        assertTrue(history.contains(event1));
        assertTrue(history.contains(event2));
        assertEquals(2, history.size());
    }

    @Test
    public void getAllLimitTo1() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        Event event3 = new Event("cccccc", "cccccc", Event.Type.ERROR);
        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        Pageable pageRequest = PageRequest.of(0, 1);
        Page<Event> results = service.findAll(pageRequest);
        assertEquals(3, results.getTotalPages());
        assertEquals(3, results.getTotalElements());
        assertEquals(0, results.getNumber());
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.getContent().contains(event3));
    }

    @Test
    public void getAllOffsetBy1() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        Event event3 = new Event("cccccc", "cccccc", Event.Type.ERROR);
        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        Pageable pageRequest = PageRequest.of(1, 1);
        Page<Event> results = service.findAll(pageRequest);
        assertEquals(3, results.getTotalPages());
        assertEquals(3, results.getTotalElements());
        assertEquals(1, results.getNumber());
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.getContent().contains(event2));
    }

    @Test
    public void getAllOffsetBy2Uneven() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        Event event3 = new Event("cccccc", "cccccc", Event.Type.ERROR);
        repository.save(event1);
        repository.save(event2);
        repository.save(event3);

        Pageable pageRequest = PageRequest.of(1, 2);
        Page<Event> results = service.findAll(pageRequest);
        assertEquals(2, results.getTotalPages());
        assertEquals(3, results.getTotalElements());
        assertEquals(1, results.getNumber());
        assertEquals(1, results.getNumberOfElements());
        assertTrue(results.getContent().contains(event1));
    }

    @Test
    public void addShouldAddEventToRepo() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        service.addAndNotify(event1);
        assertTrue(repository.findAllByOrderByTimeDesc().contains(event1));
    }

    @Test
    public void addShouldBroadcastNotification() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        service.addAndNotify(event1);
        verify(notificationService, times(1)).broadcast(event1);
    }

    @Test
    public void deleteAllShouldClearRepo() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        repository.save(event1);
        repository.save(event2);
        service.deleteAll();
        assertTrue(repository.findAllByOrderByTimeDesc().isEmpty());
    }

    @Test
    public void testGetBandwidthFromEvents() {
        ProgressListenerFactory progressListenerFactory = new ProgressListenerFactory(notificationService);
        repository.save(new Event.Builder()
                .formatDescription(progressListenerFactory.getBandwidth())
                .build());
        assertFalse(service.getBandwidthFromEvents().isEmpty());
    }

    @Test
    public void testGetNoBandwidthFromEvents() {
        assertTrue(service.getBandwidthFromEvents().isEmpty());
    }

    public class InMemoryEventRepository {
        private List<Event> data = new ArrayList<>();

        List<Event> findAll() {
            return Collections.unmodifiableList(data);
        }

        List<Event> findAllInverted() {
            return Collections.unmodifiableList(Lists.reverse(data));
        }

        Page<Event> findAllInverted(Pageable pageable) {
            int start = (int) pageable.getOffset();
            int end = (start + pageable.getPageSize()) > data.size() ? data.size() : (start + pageable.getPageSize());
            return new PageImpl<>(Lists.reverse(data).subList(start, end), pageable, data.size());
        }

        Event save(Event event) {
            data.add(event);
            return event;
        }

        void deleteAll() {
            data.clear();
        }
    }
}