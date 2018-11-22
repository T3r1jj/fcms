package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.model.Event;
import io.github.t3r1jj.fcms.backend.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<Event> getAll() {
        return historyService.findAll();
    }

    @GetMapping("/unread")
    public long countAllUnread() {
        return historyService.countAllUnread();
    }

    @GetMapping(params = "size")
    public Page<Event> getAll(
            @RequestParam int size,
            @RequestParam(defaultValue = "0", required = false) int page) {
        Pageable pageRequest = PageRequest.of(page, size);
        return historyService.findAll(pageRequest);
    }

    @DeleteMapping
    public void deleteAll() {
        historyService.deleteAll();
    }

    @PostMapping
    public void setAsRead(@RequestParam String eventId) {
        historyService.setAsRead(eventId);
    }

    @PatchMapping
    public void setAllAsRead() {
        historyService.setAllAsRead();
    }

}
