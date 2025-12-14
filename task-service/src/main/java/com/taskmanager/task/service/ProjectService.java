package com.taskmanager.task.service;

import com.taskmanager.common.exception.BusinessException;
import com.taskmanager.common.exception.ResourceNotFoundException;
import com.taskmanager.task.dto.*;
import com.taskmanager.task.entity.Project;
import com.taskmanager.task.entity.ProjectStatus;
import com.taskmanager.task.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Сервис для управления проектами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    /**
     * Создание нового проекта
     */
    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse createProject(CreateProjectRequest request, Long ownerId) {
        if (projectRepository.existsByNameAndOwnerId(request.getName(), ownerId)) {
            throw new BusinessException("Проект с таким названием уже существует");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(ownerId)
                .memberIds(request.getMemberIds() != null ? request.getMemberIds() : new ArrayList<>())
                .build();

        project = projectRepository.save(project);
        log.info("Создан проект: {} владельцем: {}", project.getName(), ownerId);

        return ProjectResponse.fromEntity(project);
    }

    /**
     * Получение проекта по ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", "id", projectId));
        return ProjectResponse.fromEntity(project);
    }

    /**
     * Обновление проекта
     */
    @Transactional
    @CacheEvict(value = "projects", key = "#projectId")
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", "id", projectId));

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getMemberIds() != null) {
            project.setMemberIds(request.getMemberIds());
        }

        project = projectRepository.save(project);
        log.info("Обновлён проект: {}", project.getId());

        return ProjectResponse.fromEntity(project);
    }

    /**
     * Удаление проекта
     */
    @Transactional
    @CacheEvict(value = "projects", key = "#projectId")
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", "id", projectId));
        projectRepository.delete(project);
        log.info("Удалён проект: {}", projectId);
    }

    /**
     * Получение проектов пользователя
     */
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsByUser(Long userId, Pageable pageable) {
        return projectRepository.findByUserAccess(userId, pageable)
                .map(ProjectResponse::fromEntity);
    }

    /**
     * Получение проектов по статусу
     */
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable)
                .map(ProjectResponse::fromEntity);
    }

    /**
     * Добавление участника в проект
     */
    @Transactional
    @CacheEvict(value = "projects", key = "#projectId")
    public ProjectResponse addMember(Long projectId, Long memberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", "id", projectId));

        if (!project.getMemberIds().contains(memberId)) {
            project.getMemberIds().add(memberId);
            project = projectRepository.save(project);
            log.info("Добавлен участник {} в проект {}", memberId, projectId);
        }

        return ProjectResponse.fromEntity(project);
    }

    /**
     * Удаление участника из проекта
     */
    @Transactional
    @CacheEvict(value = "projects", key = "#projectId")
    public ProjectResponse removeMember(Long projectId, Long memberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", "id", projectId));

        project.getMemberIds().remove(memberId);
        project = projectRepository.save(project);
        log.info("Удалён участник {} из проекта {}", memberId, projectId);

        return ProjectResponse.fromEntity(project);
    }
}
