package llc.devlab.app.repository;

import llc.devlab.app.model.entity.Label;
import llc.devlab.app.model.entity.Task;
import llc.devlab.app.model.enums.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findAllByCreatedByAndDeletedFalse(String createdBy);

    List<Task> findAllByCreatedByAndDeletedFalse(String createdBy, Pageable pageable);

    Optional<Task> findByIdAndCreatedByAndDeletedFalse(Long id, String createdBy);

    @Query("SELECT l.label FROM TaskLabel l WHERE l.task.id = :taskId")
    List<Label> findLabelsByTaskId(Long taskId);

    @Query("SELECT t FROM Task t WHERE t.deadLine <= CURRENT TIMESTAMP AND t.status != 'DONE' AND t.deleted = false")
    List<Task> getExpiredTasks();

    List<Task> findByCreatedByAndStatusAndDeletedFalse(String createdBy, TaskStatus status);
}
