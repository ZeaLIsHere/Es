package com.taskforge.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkspaceDataResponse {

    private List<WorkspaceNotificationResponse> notifications;
    private int totalCount;
    private int unreadCount;
}
