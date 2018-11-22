package io.github.t3r1jj.fcms.backend.service;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.repository.EventRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public HistoryService(EventRepository eventRepository, NotificationService notificationService, MongoTemplate mongoTemplate) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.mongoTemplate = mongoTemplate;
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

    public void setAsRead(String eventId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(eventId)));
        Update update = new Update();
        update.set("read", true);
        this.mongoTemplate.findAndModify(query, update, Event.class);
    }

    public void setAllAsRead() {
        Query query = new Query(Criteria.where("read").is(false));
        Update update = new Update();
        update.set("read", true);
        this.mongoTemplate.updateMulti(query, update, Event.class);
    }

    public long countAllUnread() {
        return eventRepository.countAllByReadFalse();
    }
}
