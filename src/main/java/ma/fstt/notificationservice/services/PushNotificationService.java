package ma.fstt.notificationservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.NotificationDTO;
import ma.fstt.notificationservice.entities.UserNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendPushNotification(UserNotification userNotification, Map<String, Object> metadata) {
        try {
            NotificationDTO dto = NotificationDTO.builder()
                    .id(userNotification.getId())
                    .userId(userNotification.getUserId())
                    .eventType(userNotification.getNotification().getEventType())
                    .title(userNotification.getNotification().getTitle())
                    .message(userNotification.getNotification().getMessage())
                    .status(userNotification.getStatus())
                    .sentAt(userNotification.getSentAt())
                    .metadata(metadata)
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + userNotification.getUserId(),
                    dto
            );

            log.info("Push notification sent to userId={}", userNotification.getUserId());

        } catch (Exception e) {
            log.error("Failed to push notification to userId={}", userNotification.getUserId(), e);
            throw e;
        }
    }
}