package ma.fstt.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.fstt.notificationservice.enums.EventType;
import ma.fstt.notificationservice.enums.Status;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private Long userId;
    private EventType eventType;
    private String title;
    private String message;
    private Status status;
    private LocalDateTime sentAt;
    private Map<String, Object> metadata;
}
