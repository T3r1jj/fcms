package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest
public class HistoryServiceIT extends AbstractTestNGSpringContextTests {

    @Autowired
    private HistoryService service;
    @Autowired
    private EventRepository repository;

    @BeforeMethod
    public void clearUp() {
        repository.deleteAll();
    }

    @AfterMethod
    public void clearDown() {
        repository.deleteAll();
    }

    @Test
    public void setAsRead() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        repository.save(event1);
        repository.save(event2);
        service.setAsRead(event1.getId().toString());
        long readCount = repository.findAll().stream().filter(Event::isRead).count();
        long unreadCount = repository.findAll().stream().filter(e -> !e.isRead()).count();
        assertEquals(1, readCount);
        assertEquals(1, unreadCount);
    }

    @Test
    public void setAllAsRead() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        repository.save(event1);
        repository.save(event2);
        long unreadCount = repository.findAll().stream().filter(e -> !e.isRead()).count();
        service.setAllAsRead();
        long readCount = repository.findAll().stream().filter(Event::isRead).count();
        assertEquals(2, unreadCount);
        assertEquals(2, readCount);
    }

    @Test
    public void countAllUnread() {
        Event event1 = new Event("a", "b", Event.Type.INFO);
        Event event2 = new Event("aaa", "bbb", Event.Type.WARNING);
        repository.save(event1);
        repository.save(event2);
        assertEquals(2, service.countAllUnread());
    }

}