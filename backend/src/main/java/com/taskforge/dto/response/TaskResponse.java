package com.taskforge.dto.response;

import com.taskforge.model.task.BaseTask;
import com.taskforge.model.task.BaseTask.Priority;
import com.taskforge.model.task.BaseTask.TaskStatus;
import com.taskforge.model.task.MilestoneTask;
import com.taskforge.model.task.SubTask;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String taskType;
    private LocalDateTime deadline;
    private boolean overdue;
    private double score;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long projectId;
    private UserResponse assignee;

    // MilestoneTask fields (null for SimpleTask)
    private List<SubTaskResponse> subTasks;
    private Integer subTaskCount;
    private Integer completedSubTaskCount;

    public static TaskResponse from(BaseTask task) {
        TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .taskType(task instanceof MilestoneTask ? "MILESTONE" : "SIMPLE")
                .deadline(task.getDeadline())
                .overdue(task.isOverdue())
                .score(task.getScore())
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .assignee(task.getAssignee() != null ? UserResponse.from(task.getAssignee()) : null);

        if (task instanceof MilestoneTask milestone) {
            List<SubTaskResponse> subTaskResponses = milestone.getSubTasks().stream()
                    .map(SubTaskResponse::from)
                    .toList();
            long done = milestone.getSubTasks().stream().filter(SubTask::isDone).count();
            builder.subTasks(subTaskResponses)
                    .subTaskCount(milestone.getSubTasks().size())
                    .completedSubTaskCount((int) done);
        }

        return builder.build();
    }

    @Getter
    @Builder
    public static class SubTaskResponse {
        private Long id;
        private String title;
        private boolean done;

        public static SubTaskResponse from(SubTask st) {
            return SubTaskResponse.builder()
                    .id(st.getId())
                    .title(st.getTitle())
                    .done(st.isDone())
                    .build();
        }
    }
}
