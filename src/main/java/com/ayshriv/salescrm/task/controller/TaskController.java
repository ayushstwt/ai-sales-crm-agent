package com.ayshriv.salescrm.task.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.task.dto.TaskCreateRequest;
import com.ayshriv.salescrm.task.dto.TaskSearchRequest;
import com.ayshriv.salescrm.task.dto.TaskUpdateRequest;
import com.ayshriv.salescrm.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listTasks(@ModelAttribute TaskSearchRequest request) {
        ApiStatus status = taskService.listTasks(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "tasks", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewTask(@PathVariable Long id) {
        ApiStatus status = taskService.viewTask(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "task"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createTask(@Valid @RequestBody TaskCreateRequest request) {
        ApiStatus status = taskService.createTask(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "task"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editTask(@PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        ApiStatus status = taskService.editTask(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "task"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteTask(@PathVariable Long id) {
        ApiStatus status = taskService.deleteTask(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}