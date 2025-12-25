package ma.fstt.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.fstt.notificationservice.enums.Channel;
import ma.fstt.notificationservice.enums.EventType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent implements Serializable {

    private EventType eventType;
    private List<Long> userIds;
    private String title;
    private String message;
    private List<Channel> channels;
    private Map<String, Object> metadata;
}