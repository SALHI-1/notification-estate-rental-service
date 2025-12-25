package ma.fstt.notificationservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.NotificationEvent;
import ma.fstt.notificationservice.entities.DeadLetterQueue;
import ma.fstt.notificationservice.repositories.DeadLetterQueueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadLetterQueueService {

    private final DeadLetterQueueRepository dlqRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${dlq.max-retries:5}")
    private Integer maxRetries;

    @Transactional
    public void saveToDeadLetterQueue(String topic, Integer partition, Long offset,
                                      String payload, String errorMessage, String stackTrace) {
        DeadLetterQueue dlq = DeadLetterQueue.builder()
                .topic(topic)
                .partition(partition)
                .kafkaOffset(offset)
                .payload(payload)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .retryCount(0)
                .processed(false)
                .build();

        dlqRepository.save(dlq);
        log.info("Message saved to DLQ: topic={}, partition={}, offset={}", topic, partition, offset);
    }

    @Scheduled(fixedDelayString = "${dlq.retry-interval:300000}") // 5 minutes par défaut
    @Transactional
    public void retryFailedMessages() {
        List<DeadLetterQueue> failedMessages =
                dlqRepository.findByProcessedFalseAndRetryCountLessThan(maxRetries);

        if (failedMessages.isEmpty()) {
            return;
        }

        log.info("Found {} failed messages to retry", failedMessages.size());

        for (DeadLetterQueue dlq : failedMessages) {
            try {
                log.info("Retrying message: id={}, retryCount={}", dlq.getId(), dlq.getRetryCount());

                // Désérialiser le payload
                NotificationEvent event = objectMapper.readValue(dlq.getPayload(), NotificationEvent.class);

                // Réessayer le traitement
                notificationService.processNotification(event);

                // Marquer comme traité
                dlq.setProcessed(true);
                dlq.setProcessedAt(LocalDateTime.now());
                dlqRepository.save(dlq);

                log.info("Successfully reprocessed message: id={}", dlq.getId());

            } catch (Exception e) {
                log.error("Failed to reprocess message: id={}", dlq.getId(), e);

                // Incrémenter le compteur de tentatives
                dlq.setRetryCount(dlq.getRetryCount() + 1);
                dlq.setErrorMessage(e.getMessage());
                dlqRepository.save(dlq);

                if (dlq.getRetryCount() >= maxRetries) {
                    log.error("Message exceeded max retries: id={}, retries={}",
                            dlq.getId(), dlq.getRetryCount());
                }
            }
        }
    }

    public Long getUnprocessedCount() {
        return dlqRepository.countByProcessedFalse();
    }

    public List<DeadLetterQueue> getOldUnprocessedMessages(int daysOld) {
        LocalDateTime before = LocalDateTime.now().minusDays(daysOld);
        return dlqRepository.findOldUnprocessedMessages(before);
    }
}