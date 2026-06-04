package com.taskforge.service;

import com.taskforge.dto.request.TaskAssignRequest;
import com.taskforge.dto.request.TaskRequest;
import com.taskforge.dto.request.TaskStatusRequest;
import com.taskforge.dto.response.TaskResponse;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.exception.ValidationException;
import com.taskforge.model.ActivityLog.ActionType;
import com.taskforge.model.Project;
import com.taskforge.model.User;
import com.taskforge.model.task.BaseTask;
import com.taskforge.model.task.BaseTask.TaskStatus;
import com.taskforge.model.task.MilestoneTask;
import com.taskforge.model.task.SimpleTask;
import com.taskforge.repository.TaskRepository;
import com.taskforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final ActivityLogService activityLogService;
    private final ContributionService contributionService;

    // ── SpEL helper (called from @PreAuthorize) ───────────────────────────

    @Transactional(readOnly = true)
    public boolean isAssignee(Long taskId, String email) {
        return taskRepository.findById(taskId)
                .map(t -> t.getAssignee() != null && t.getAssignee().getEmail().equals(email))
                .orElse(false);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────

    @Transactional
    public TaskResponse createTask(Long projectId, String actorEmail, TaskRequest request) {
        Project project = projectService.getProject(projectId);
        User actor = projectService.getUser(actorEmail);

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));

        boolean assigneeInProject = project.getOwner().getId().equals(assignee.getId())
                || project.getMembers().contains(assignee);
        if (!assigneeInProject) {
            throw new ValidationException("Assignee bukan anggota proyek ini");
        }

        BaseTask task = request.getTaskType() == TaskRequest.TaskType.MILESTONE
                ? new MilestoneTask()
                : new SimpleTask();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setProject(project);
        task.reassign(assignee);

        BaseTask saved = taskRepository.save(task);

        activityLogService.log(project, actor,
                actor.getName() + " membuat task '" + saved.getTitle() + "' dan assign ke " + assignee.getName(),
                ActionType.TASK_CREATED);

        log.info("Task '{}' dibuat di proyek id={}", saved.getTitle(), projectId);
        return TaskResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getProjectTasks(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional
    public TaskResponse updateStatus(Long taskId, String actorEmail, TaskStatusRequest request) {
        BaseTask task = getTask(taskId);
        User actor = projectService.getUser(actorEmail);

        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = request.getStatus();

        if (newStatus == TaskStatus.DONE) {
            task.complete();
        } else {
            task.setStatus(newStatus);
            task.setCompletedAt(null);
        }

        BaseTask saved = taskRepository.save(task);

        // REQ-SCORE-01: recompute contribution score whenever a task is marked DONE
        if (newStatus == TaskStatus.DONE) {
            contributionService.addScore(saved);
        }

        activityLogService.log(task.getProject(), actor,
                actor.getName() + " mengubah status task '" + task.getTitle()
                        + "' dari " + oldStatus + " ke " + newStatus,
                ActionType.STATUS_CHANGED);

        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, String actorEmail, TaskAssignRequest request) {
        BaseTask task = getTask(taskId);
        User actor = projectService.getUser(actorEmail);

        User newAssignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));

        Project project = task.getProject();
        boolean inProject = project.getOwner().getId().equals(newAssignee.getId())
                || project.getMembers().contains(newAssignee);
        if (!inProject) {
            throw new ValidationException("Assignee bukan anggota proyek ini");
        }

        String oldName = task.getAssignee() != null ? task.getAssignee().getName() : "tidak ada";
        task.reassign(newAssignee);
        BaseTask saved = taskRepository.save(task);

        activityLogService.log(project, actor,
                actor.getName() + " mengubah assignee task '" + task.getTitle()
                        + "' dari " + oldName + " ke " + newAssignee.getName(),
                ActionType.ASSIGNEE_CHANGED);

        return TaskResponse.from(saved);
    }

    @Transactional
    public void deleteTask(Long taskId, String actorEmail) {
        BaseTask task = getTask(taskId);
        User actor = projectService.getUser(actorEmail);

        activityLogService.log(task.getProject(), actor,
                actor.getName() + " menghapus task '" + task.getTitle() + "'",
                ActionType.TASK_CREATED);

        taskRepository.delete(task);
        log.info("Task id={} dihapus oleh {}", taskId, actorEmail);
    }

    // ── Internal ──────────────────────────────────────────────────────────

    public BaseTask getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }
}
