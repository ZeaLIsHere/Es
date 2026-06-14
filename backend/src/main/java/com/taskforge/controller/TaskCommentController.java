package com.taskforge.controller;

import com.taskforge.common.ApiResponse;
import com.taskforge.dto.request.TaskCommentRequest;
import com.taskforge.dto.response.TaskCommentResponse;
import com.taskforge.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks/{taskId}/comments")
public class TaskCommentController {

    private final TaskCommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(
            @PathVariable Long taskId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getComments(taskId, auth.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Komentar ditambahkan", commentService.addComment(taskId, auth.getName(), request)));
    }
}
