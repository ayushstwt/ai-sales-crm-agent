package com.ayshriv.salescrm.task.service.impl;

import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.service.AuditLogService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.task.dto.TaskCreateRequest;
import com.ayshriv.salescrm.task.dto.TaskSearchRequest;
import com.ayshriv.salescrm.task.dto.TaskUpdateRequest;
import com.ayshriv.salescrm.task.entity.Task;
import com.ayshriv.salescrm.task.entity.TaskPriority;
import com.ayshriv.salescrm.task.entity.TaskStatus;
import com.ayshriv.salescrm.task.repository.TaskRepository;
import com.ayshriv.salescrm.task.service.TaskService;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final LogService logService;
    private final AuditLogService auditLogService;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            LogService logService,
            AuditLogService auditLogService
    ) {
        this.taskRepository = taskRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listTasks(TaskSearchRequest request) {
        LOGGER.info("TaskService >> listTasks called!");
        try {
            request = (TaskSearchRequest) Resources.getDefaultRequest(request);
            TenantContext context = tenantContextService.getCurrentContext();

            Sort.Direction direction = "DESC".equalsIgnoreCase(request.getOrderDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            String orderBy = (request.getOrderBy() != null && !request.getOrderBy().isBlank())
                    ? request.getOrderBy()
                    : "id";

            Pageable pageable = PageRequest.of(
                    Math.max(0, request.getPageNumber() - 1),
                    request.getPageSize(),
                    Sort.by(direction, orderBy)
            );

            Long targetOrgId = request.getOrganizationId();
            if (targetOrgId == null && context != null) {
                targetOrgId = context.getOrganizationId();
            }

            Page<Task> page;
            if (targetOrgId != null) {
                if (request.getStatus() != null) {
                    page = taskRepository.findByOrganizationIdAndStatusAndIsDeletedFalse(targetOrgId, request.getStatus(), pageable);
                } else if (request.getAssignedToId() != null) {
                    page = taskRepository.findByOrganizationIdAndAssignedToIdAndIsDeletedFalse(targetOrgId, request.getAssignedToId(), pageable);
                } else {
                    page = taskRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
                }
            } else {
                page = taskRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.TASK);
            status.setTasks(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("TaskService >> listTasks exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewTask(Long id) {
        LOGGER.info("TaskService >> viewTask called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Task> taskOptional;
            if (context != null && context.getOrganizationId() != null) {
                taskOptional = taskRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                taskOptional = taskRepository.findByIdAndIsDeletedFalse(id);
            }

            if (taskOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.TASK);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.TASK);
            status.setTask(taskOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("TaskService >> viewTask exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createTask(TaskCreateRequest request) {
        return createTask(request, AuditSource.API);
    }

    @Override
    @Transactional
    public ApiStatus createTask(TaskCreateRequest request, AuditSource auditSource) {
        LOGGER.info("TaskService >> createTask called for title: {}, source: {}", request != null ? request.getTitle() : null, auditSource);
        try {
            if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "title", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Long targetOrgId = request.getOrganizationId();
            if (targetOrgId == null && context != null) {
                targetOrgId = context.getOrganizationId();
            }

            if (targetOrgId == null) {
                return Resources.setStatus(Constants.FAILURE, "Organization ID is required.", null);
            }

            Optional<Organization> orgOptional = organizationRepository.findByIdAndIsDeletedFalse(targetOrgId);
            if (orgOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Organization not found.", null);
            }

            Organization organization = orgOptional.get();
            User currentUser = resolveCurrentUser(context);

            Task task = new Task();
            task.setOrganization(organization);
            task.setTitle(request.getTitle().trim());
            task.setDescription(request.getDescription());
            task.setDueDate(request.getDueDate());
            task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING);
            task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
            task.setRelatedType(request.getRelatedType());
            task.setRelatedId(request.getRelatedId());
            task.setCreatedBy(currentUser);
            task.setIsActive(true);
            task.setIsDeleted(false);
            task.setCreatedOn(LocalDateTime.now());

            if (request.getAssignedToId() != null) {
                userRepository.findByIdAndIsDeletedFalse(request.getAssignedToId()).ifPresent(task::setAssignedTo);
            } else if (currentUser != null) {
                task.setAssignedTo(currentUser);
            }

            Task savedTask = taskRepository.save(task);

            logService.createLog(currentUser, LogConstants.TASK, LogConstants.ADD, LocalDateTime.now(), null);
            auditLogService.logAction(
                    organization,
                    currentUser,
                    LogConstants.TASK,
                    savedTask.getId(),
                    LogConstants.ADD,
                    auditSource != null ? auditSource : AuditSource.API,
                    "Created task: " + savedTask.getTitle() + " (Priority: " + savedTask.getPriority() + ")"
            );

            ApiStatus status = Resources.setStatus(Constants.CREATED, Constants.SAVE_SUCCESS, LogConstants.TASK);
            status.setTask(savedTask);
            return status;

        } catch (Exception e) {
            LOGGER.error("TaskService >> createTask exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editTask(Long id, TaskUpdateRequest request) {
        LOGGER.info("TaskService >> editTask called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Task> taskOptional;
            if (context != null && context.getOrganizationId() != null) {
                taskOptional = taskRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                taskOptional = taskRepository.findByIdAndIsDeletedFalse(id);
            }

            if (taskOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.TASK);
            }

            Task task = taskOptional.get();
            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                task.setTitle(request.getTitle().trim());
            }
            if (request.getDescription() != null) {
                task.setDescription(request.getDescription());
            }
            if (request.getDueDate() != null) {
                task.setDueDate(request.getDueDate());
            }
            if (request.getStatus() != null) {
                task.setStatus(request.getStatus());
            }
            if (request.getPriority() != null) {
                task.setPriority(request.getPriority());
            }
            if (request.getRelatedType() != null) {
                task.setRelatedType(request.getRelatedType());
            }
            if (request.getRelatedId() != null) {
                task.setRelatedId(request.getRelatedId());
            }
            if (request.getAssignedToId() != null) {
                userRepository.findByIdAndIsDeletedFalse(request.getAssignedToId()).ifPresent(task::setAssignedTo);
            }

            task.setUpdatedOn(LocalDateTime.now());
            Task updatedTask = taskRepository.save(task);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.TASK, LogConstants.EDIT, LocalDateTime.now(), null);
            auditLogService.logAction(
                    updatedTask.getOrganization(),
                    currentUser,
                    LogConstants.TASK,
                    updatedTask.getId(),
                    LogConstants.EDIT,
                    AuditSource.API,
                    "Updated task: " + updatedTask.getTitle()
            );

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.TASK);
            status.setTask(updatedTask);
            return status;

        } catch (Exception e) {
            LOGGER.error("TaskService >> editTask exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteTask(Long id) {
        LOGGER.info("TaskService >> deleteTask called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Task> taskOptional;
            if (context != null && context.getOrganizationId() != null) {
                taskOptional = taskRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                taskOptional = taskRepository.findByIdAndIsDeletedFalse(id);
            }

            if (taskOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.TASK);
            }

            Task task = taskOptional.get();
            task.setIsDeleted(true);
            task.setUpdatedOn(LocalDateTime.now());
            taskRepository.save(task);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.TASK, LogConstants.DELETE, LocalDateTime.now(), null);
            auditLogService.logAction(
                    task.getOrganization(),
                    currentUser,
                    LogConstants.TASK,
                    task.getId(),
                    LogConstants.DELETE,
                    AuditSource.API,
                    "Soft-deleted task: " + task.getTitle()
            );

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.TASK);

        } catch (Exception e) {
            LOGGER.error("TaskService >> deleteTask exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    private User resolveCurrentUser(TenantContext context) {
        if (context != null && context.getUserId() != null) {
            return userRepository.findById(context.getUserId()).orElse(null);
        }
        return null;
    }
}