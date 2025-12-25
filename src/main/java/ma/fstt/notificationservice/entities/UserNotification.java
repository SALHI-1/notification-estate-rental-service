package ma.fstt.notificationservice.entities;

import jakarta.persistence.*;
import lombok.*;
import ma.fstt.notificationservice.enums.Channel;
import ma.fstt.notificationservice.enums.Status;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = Status.UNREAD;
        }
    }
}
