package com.taskforge.dto.response;

import com.taskforge.model.ActivityLog;
import com.taskforge.model.ActivityLog.ActionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorkspaceNotificationResponse {

    private Long id;
    private String message;
    private String projectTitle;
    private Long projectId;
    private String actorName;
    private ActionType actionType;
    private LocalDateTime timestamp;

    public static WorkspaceNotificationResponse from(ActivityLog log) {
        return WorkspaceNotificationResponse.builder()
                .id(log.getId())
                .message(log.getAction())
                .projectTitle(log.getProject().getTitle())
                .projectId(log.getProject().getId())
                .actorName(log.getActor().getName())
                .actionType(log.getActionType())
                .timestamp(log.getTimestamp())
                .build();
    }
}
