package com.taskmanager.task.repository;

import com.taskmanager.task.entity.Project;
import com.taskmanager.task.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с проектами
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.ownerId = :userId OR :userId MEMBER OF p.memberIds")
    Page<Project> findByUserAccess(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE (p.ownerId = :userId OR :userId MEMBER OF p.memberIds) AND p.status = :status")
    Page<Project> findByUserAccessAndStatus(@Param("userId") Long userId, @Param("status") ProjectStatus status, Pageable pageable);

    List<Project> findByOwnerIdAndStatus(Long ownerId, ProjectStatus status);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
