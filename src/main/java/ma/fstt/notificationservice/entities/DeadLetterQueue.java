package ma.fstt.notificationservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dead_letter_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private Integer partition;

    @Column(name = "kafka_offset", nullable = false)
    private Long kafkaOffset;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    @Column
    private LocalDateTime processedAt;

    @Column(nullable = false)
    private Boolean processed;

    @PrePersist
    public void prePersist() {
        this.failedAt = LocalDateTime.now();
        this.processed = false;
        if (this.retryCount == null) this.retryCount = 0;
    }
}