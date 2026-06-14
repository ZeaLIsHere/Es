package com.taskforge.dto.response;

import com.taskforge.model.task.TaskComment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskCommentResponse {
    private Long id;
    private String userName;
    private String userRole;
    private String content;
    private LocalDateTime createdAt;

    public static TaskCommentResponse from(TaskComment comment) {
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .userName(comment.getUser().getName())
                .userRole(comment.getUser().getRole().name())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
