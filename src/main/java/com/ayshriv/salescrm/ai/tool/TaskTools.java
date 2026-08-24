package com.ayshriv.salescrm.ai.tool;

import com.ayshriv.salescrm.ai.service.ToolExecutionService;
import com.ayshriv.salescrm.ai.tool.dto.CreateTaskInput;
import com.ayshriv.salescrm.ai.tool.dto.CreateTaskOutput;
import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.task.dto.TaskCreateRequest;
import com.ayshriv.salescrm.task.entity.Task;
import com.ayshriv.salescrm.task.entity.TaskPriority;
import com.ayshriv.salescrm.task.entity.TaskStatus;
import com.ayshriv.salescrm.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * AI Tool for Task operations.
 * 
 * Enforces master.md §7 Rule #2:
 * Flow: AI → Tool → TaskService (Application Service) → TenantContext/Authorization → Repository.
 * Writes audit_logs with source = AI_AGENT (Step 5.7).
 */
@Component
public class TaskTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTools.class);

    private final TaskService taskService;
    private final ToolExecutionService toolExecutionService;
    private final ObjectMapper objectMapper;

    public TaskTools(
            TaskService taskService,
            ToolExecutionService toolExecutionService,
            ObjectMapper objectMapper
    ) {
        this.taskService = taskService;
        this.toolExecutionService = toolExecutionService;
        this.objectMapper = objectMapper;
    }

    public CreateTaskOutput createTask(CreateTaskInput input) {
        LOGGER.info("TaskTools >> createTask called for title: {}", input != null ? input.getTitle() : null);
        long startTime = System.currentTimeMillis();
        String inputJson = serialize(input);

        try {
            if (input == null || input.getTitle() == null || input.getTitle().isBlank()) {
                return new CreateTaskOutput(false, null, null, null, null, "Task title is required.");
            }

            TaskCreateRequest request = new TaskCreateRequest();
            request.setTitle(input.getTitle().trim());
            request.setDescription(input.getDescription());
            request.setAssignedToId(input.getAssignedToId());
            request.setRelatedType(input.getRelatedType());
            request.setRelatedId(input.getRelatedId());

            if (input.getPriority() != null && !input.getPriority().isBlank()) {
                try {
                    request.setPriority(TaskPriority.valueOf(input.getPriority().trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("TaskTools >> Invalid priority: {}", input.getPriority());
                }
            }

            if (input.getStatus() != null && !input.getStatus().isBlank()) {
                try {
                    request.setStatus(TaskStatus.valueOf(input.getStatus().trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("TaskTools >> Invalid status: {}", input.getStatus());
                }
            }

            if (input.getDueDate() != null && !input.getDueDate().isBlank()) {
                try {
                    request.setDueDate(LocalDateTime.parse(input.getDueDate().trim()));
                } catch (Exception e) {
                    LOGGER.warn("TaskTools >> Invalid due date format: {}", input.getDueDate());
                }
            }

            // CRITICAL: Call the Stage 2 Application Service with AuditSource.AI_AGENT
            ApiStatus status = taskService.createTask(request, AuditSource.AI_AGENT);

            if (status.getTask() == null) {
                CreateTaskOutput output = new CreateTaskOutput(false, null, input.getTitle(), null, null, status.getText());
                long duration = System.currentTimeMillis() - startTime;
                toolExecutionService.recordExecution("createTask", inputJson, serialize(output), "FAILURE", duration);
                return output;
            }

            Task saved = status.getTask();
            CreateTaskOutput output = new CreateTaskOutput(
                    true,
                    saved.getId(),
                    saved.getTitle(),
                    saved.getStatus() != null ? saved.getStatus().name() : null,
                    saved.getPriority() != null ? saved.getPriority().name() : null,
                    "Task created successfully."
            );

            long duration = System.currentTimeMillis() - startTime;
            toolExecutionService.recordExecution("createTask", inputJson, serialize(output), "SUCCESS", duration);
            return output;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.error("TaskTools >> createTask failed: {}", e.getMessage(), e);
            toolExecutionService.recordExecution("createTask", inputJson, "Error: " + e.getMessage(), "ERROR", duration);
            return new CreateTaskOutput(false, null, input != null ? input.getTitle() : null, null, null, "Error: " + e.getMessage());
        }
    }

    public FunctionCallback createTaskFunctionCallback() {
        return FunctionCallbackWrapper.builder((Function<CreateTaskInput, CreateTaskOutput>) this::createTask)
                .withName("createTask")
                .withDescription("Create a new CRM task or follow-up action. Supports title, description, dueDate, priority (LOW, MEDIUM, HIGH, URGENT), relatedType (LEAD, CONTACT, COMPANY, DEAL), and relatedId.")
                .withInputType(CreateTaskInput.class)
                .build();
    }

    private String serialize(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}