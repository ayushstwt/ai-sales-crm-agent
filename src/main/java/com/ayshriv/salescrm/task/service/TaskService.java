package com.ayshriv.salescrm.task.service;

import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.task.dto.TaskCreateRequest;
import com.ayshriv.salescrm.task.dto.TaskSearchRequest;
import com.ayshriv.salescrm.task.dto.TaskUpdateRequest;

public interface TaskService {

    ApiStatus listTasks(TaskSearchRequest request);

    ApiStatus viewTask(Long id);

    ApiStatus createTask(TaskCreateRequest request);

    ApiStatus createTask(TaskCreateRequest request, AuditSource auditSource);

    ApiStatus editTask(Long id, TaskUpdateRequest request);

    ApiStatus deleteTask(Long id);
}