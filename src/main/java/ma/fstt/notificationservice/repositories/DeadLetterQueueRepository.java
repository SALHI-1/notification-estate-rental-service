package ma.fstt.notificationservice.repositories;

import ma.fstt.notificationservice.entities.DeadLetterQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DeadLetterQueueRepository extends JpaRepository<DeadLetterQueue, Long> {

    List<DeadLetterQueue> findByProcessedFalseAndRetryCountLessThan(Integer maxRetries);

    @Query("SELECT d FROM DeadLetterQueue d WHERE d.processed = false AND d.failedAt < :before")
    List<DeadLetterQueue> findOldUnprocessedMessages(LocalDateTime before);

    Long countByProcessedFalse();
}