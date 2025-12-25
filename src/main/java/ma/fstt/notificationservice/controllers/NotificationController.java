package ma.fstt.notificationservice.controllers;

import lombok.RequiredArgsConstructor;
import ma.fstt.notificationservice.entities.DeadLetterQueue;
import ma.fstt.notificationservice.services.DeadLetterQueueService;
import ma.fstt.notificationservice.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final DeadLetterQueueService dlqService;

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dlq/count")
    public ResponseEntity<Long> getDlqCount() {
        return ResponseEntity.ok(dlqService.getUnprocessedCount());
    }

    @GetMapping("/dlq/old")
    public ResponseEntity<List<DeadLetterQueue>> getOldDlqMessages(
            @RequestParam(defaultValue = "7") int daysOld) {
        return ResponseEntity.ok(dlqService.getOldUnprocessedMessages(daysOld));
    }

    @PostMapping("/dlq/retry")
    public ResponseEntity<Void> retryDlqMessages() {
        dlqService.retryFailedMessages();
        return ResponseEntity.ok().build();
    }
}