package ma.fstt.notificationservice.repositories;

import ma.fstt.notificationservice.entities.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification , Long> {

}
