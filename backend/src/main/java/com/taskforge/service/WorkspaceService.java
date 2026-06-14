package com.taskforge.service;

import com.taskforge.dto.response.WorkspaceDataResponse;
import com.taskforge.dto.response.WorkspaceNotificationResponse;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.model.ActivityLog;
import com.taskforge.model.Project;
import com.taskforge.model.User;
import com.taskforge.repository.ActivityLogRepository;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private static final int MAX_NOTIFICATIONS = 50;

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional(readOnly = true)
    public WorkspaceDataResponse getWorkspaceData(String actorEmail) {
        User user = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        List<Project> projects = projectRepository.findAllByMemberOrOwner(user);
        if (projects.isEmpty()) {
            return WorkspaceDataResponse.builder()
                    .notifications(Collections.emptyList())
                    .totalCount(0)
                    .unreadCount(0)
                    .build();
        }

        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        List<ActivityLog> logs = activityLogRepository
                .findByProjectIdInOrderByTimestampDesc(projectIds);

        List<WorkspaceNotificationResponse> notifications = logs.stream()
                .limit(MAX_NOTIFICATIONS)
                .map(WorkspaceNotificationResponse::from)
                .toList();

        int totalCount = logs.size();
        int unreadCount = Math.min(totalCount, 10);

        log.debug("Workspace data loaded for {}: {} notifications", actorEmail, totalCount);

        return WorkspaceDataResponse.builder()
                .notifications(notifications)
                .totalCount(totalCount)
                .unreadCount(unreadCount)
                .build();
    }
}
