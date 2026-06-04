package com.taskforge.controller;

import com.taskforge.common.ApiResponse;
import com.taskforge.dto.request.TaskAssignRequest;
import com.taskforge.dto.request.TaskRequest;
import com.taskforge.dto.request.TaskStatusRequest;
import com.taskforge.dto.response.TaskResponse;
import com.taskforge.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/api/projects/{projectId}/tasks")
    @PreAuthorize("@projectService.isMember(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getProjectTasks(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getProjectTasks(projectId)));
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @PreAuthorize("hasRole('KETUA') and @projectService.isOwner(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            Authentication auth) {
        TaskResponse task = taskService.createTask(projectId, auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Task berhasil dibuat", task));
    }

    // ANGGOTA can only update status of tasks assigned to them; KETUA can update any
    @PutMapping("/api/tasks/{taskId}/status")
    @PreAuthorize("hasRole('KETUA') or @taskService.isAssignee(#taskId, authentication.name)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Status diperbarui",
                taskService.updateStatus(taskId, auth.getName(), request)));
    }

    @PutMapping("/api/tasks/{taskId}/assign")
    @PreAuthorize("hasRole('KETUA')")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskAssignRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Task di-assign",
                taskService.assignTask(taskId, auth.getName(), request)));
    }

    @DeleteMapping("/api/tasks/{taskId}")
    @PreAuthorize("hasRole('KETUA')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId, Authentication auth) {
        taskService.deleteTask(taskId, auth.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }
}
