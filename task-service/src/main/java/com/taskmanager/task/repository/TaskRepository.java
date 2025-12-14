package com.taskmanager.task.repository;

import com.taskmanager.task.entity.Task;
import com.taskmanager.task.entity.TaskPriority;
import com.taskmanager.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с задачами
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    Page<Task> findByCreatorId(Long creatorId, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Page<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId AND t.status IN :statuses")
    Page<Task> findByAssigneeIdAndStatusIn(@Param("assigneeId") Long assigneeId, @Param("statuses") List<TaskStatus> statuses, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.deadline < :deadline AND t.status NOT IN :excludeStatuses")
    List<Task> findOverdueTasks(@Param("deadline") LocalDateTime deadline, @Param("excludeStatuses") List<TaskStatus> excludeStatuses);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId ORDER BY t.priority DESC, t.deadline ASC")
    List<Task> findByProjectIdOrderByPriorityAndDeadline(@Param("projectId") Long projectId);
}
