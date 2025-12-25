package ma.fstt.notificationservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.NotificationEvent;
import ma.fstt.notificationservice.services.NotificationService;
import ma.fstt.notificationservice.services.DeadLetterQueueService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final DeadLetterQueueService dlqService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.notification}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(
            ConsumerRecord<String, NotificationEvent> record,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received notification event: topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key());

            NotificationEvent event = record.value();

            // Validation de base
            if (event == null || event.getUserIds() == null || event.getUserIds().isEmpty()) {
                log.warn("Invalid notification event received, skipping");
                acknowledgment.acknowledge();
                return;
            }

            // Traitement de la notification
            notificationService.processNotification(event);

            // Acknowledge après traitement réussi
            acknowledgment.acknowledge();
            log.info("Successfully processed notification for users: {}", event.getUserIds());

        } catch (Exception e) {
            log.error("Error processing notification event: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset(), e);

            try {
                // Envoi vers DLQ
                String payload = objectMapper.writeValueAsString(record.value());
                dlqService.saveToDeadLetterQueue(
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        payload,
                        e.getMessage(),
                        getStackTrace(e)
                );

                // Acknowledge pour ne pas bloquer le consumer
                acknowledgment.acknowledge();
                log.info("Message sent to DLQ: topic={}, offset={}", record.topic(), record.offset());

            } catch (Exception dlqException) {
                log.error("Failed to save message to DLQ", dlqException);
                // Ne pas acknowledger pour que Kafka retry
            }
        }
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}