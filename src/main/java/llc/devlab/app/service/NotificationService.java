package llc.devlab.app.service;

import llc.devlab.app.model.entity.TaskNotification;
import llc.devlab.app.repository.NotificationRepository;
import llc.devlab.app.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class NotificationService {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void sendNotification() {
        taskRepository.getExpiredTasks().forEach(task -> {
            TaskNotification notification = new TaskNotification();
            notification.setTask(task);
            notification.setUserEmail(task.getCreatedBy());
            notificationRepository.save(notification);
        });
    }
}
