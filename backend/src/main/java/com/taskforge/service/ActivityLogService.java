package com.taskforge.service;

import com.taskforge.model.ActivityLog;
import com.taskforge.model.ActivityLog.ActionType;
import com.taskforge.model.Project;
import com.taskforge.model.User;
import com.taskforge.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void log(Project project, User actor, String action, ActionType actionType) {
        ActivityLog entry = ActivityLog.builder()
                .project(project)
                .actor(actor)
                .action(action)
                .actionType(actionType)
                .build();
        activityLogRepository.save(entry);
        log.debug("Activity logged: [{}] {} by {}", actionType, action, actor.getEmail());
    }
}
