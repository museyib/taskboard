package llc.devlab.app.repository;

import llc.devlab.app.model.entity.TaskNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<TaskNotification, Long> {
}
