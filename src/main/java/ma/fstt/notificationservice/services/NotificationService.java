package ma.fstt.notificationservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.notificationservice.dto.NotificationEvent;
import ma.fstt.notificationservice.entities.Notification;
import ma.fstt.notificationservice.entities.UserNotification;
import ma.fstt.notificationservice.enums.Channel;
import ma.fstt.notificationservice.enums.Status;
import ma.fstt.notificationservice.repositories.NotificationRepository;
import ma.fstt.notificationservice.repositories.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public void processNotification(NotificationEvent event) {

        log.info("Processing notification event: type={}, users={}",
                event.getEventType(), event.getUserIds().size());

        Notification notification = Notification.builder()
                .eventType(event.getEventType())
                .title(event.getTitle())
                .message(event.getMessage())
                .build();

        List<UserNotification> userNotifications = new ArrayList<>();

        for (Long userId : event.getUserIds()) {
            for (Channel channel : event.getChannels()) {

                UserNotification un = UserNotification.builder()
                        .notification(notification)
                        .userId(userId)
                        .channel(channel)
                        .status(Status.UNREAD)
                        .build();

                userNotifications.add(un);
            }
        }

        notification.setUserNotifications(userNotifications);
        notificationRepository.save(notification);

        sendNotifications(event, userNotifications);
    }

    private void sendNotifications(NotificationEvent event, List<UserNotification> userNotifications) {

        for (UserNotification un : userNotifications) {
            try {
                if (un.getChannel() == Channel.PUSH) {
                    pushNotificationService.sendPushNotification(un, event.getMetadata());
                }

                un.setStatus(Status.UNREAD);
                un.setSentAt(LocalDateTime.now());

                log.info("Notification sent: userId={}, channel={}", un.getUserId(), un.getChannel());

            } catch (Exception e) {
                un.setStatus(Status.FAILED);
                log.error("Failed to send notification userId={} channel={}", un.getUserId(), un.getChannel(), e);
            }
        }

        userNotificationRepository.saveAll(userNotifications);
    }

    @Transactional
    public void markAsRead(Long userNotificationId) {
        userNotificationRepository.findById(userNotificationId)
                .ifPresent(userNotification -> {
                    userNotification.setStatus(Status.READ);
                    log.info("Notification marked as read: id={}", userNotificationId);
                });
    }
}