package com.taskforge.dto.response;

import com.taskforge.model.ActivityLog;
import com.taskforge.model.ActivityLog.ActionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ActivityLogResponse {

    private Long id;
    private String action;
    private ActionType actionType;
    private LocalDateTime timestamp;
    private String actorName;
    private String actorEmail;

    public static ActivityLogResponse from(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .actionType(log.getActionType())
                .timestamp(log.getTimestamp())
                .actorName(log.getActor().getName())
                .actorEmail(log.getActor().getEmail())
                .build();
    }
}
