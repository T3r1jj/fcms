package io.github.t3r1jj.fcms.backend.controller;

import io.github.t3r1jj.fcms.backend.controller.exception.UnprocessableException;
import io.github.t3r1jj.fcms.backend.model.StoredRecord;
import io.github.t3r1jj.fcms.backend.model.StoredRecordMeta;
import io.github.t3r1jj.fcms.backend.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

@RestController
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    @Autowired
    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public void uploadFile(@RequestParam MultipartFile file, @RequestParam String name, @RequestParam String tag,
                           @RequestParam(required = false) String parentId) throws Exception {
        if (!file.isEmpty()) {
            byte[] data = file.getBytes();
            recordService.store(new StoredRecord(name, tag, data, parentId));
        } else {
            throw new UnprocessableException("Empty file");
        }
    }

    @PutMapping
    public void updateMeta(@RequestBody StoredRecordMeta meta) {
        recordService.updateMeta(meta);
    }

    @GetMapping
    public Collection<StoredRecord> getStoredRecords() {
        return recordService.findAll();
    }

    @DeleteMapping
    public void deleteStoredRecords(@RequestParam String id) {
        recordService.delete(id, false);
    }

    @PatchMapping
    public void forceDeleteStoredRecords(@RequestParam String id) {
        recordService.delete(id, true);
    }

}
