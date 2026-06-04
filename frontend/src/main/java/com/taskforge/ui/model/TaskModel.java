package com.taskforge.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskModel {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String taskType;
    private LocalDateTime deadline;
    private boolean overdue;
    private double score;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long projectId;
    private UserModel assignee;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public UserModel getAssignee() { return assignee; }
    public void setAssignee(UserModel assignee) { this.assignee = assignee; }

    public String getDeadlineLabel() {
        if (deadline == null) return "Tidak ada deadline";
        if (overdue) return "OVERDUE";
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
        if (days == 0) return "Hari ini";
        if (days == 1) return "Besok";
        return days + " hari lagi";
    }

    public String getAssigneeName() {
        return assignee != null ? assignee.getName() : "Belum di-assign";
    }

    public String getNextStatus() {
        return switch (status) {
            case "TODO" -> "IN_PROGRESS";
            case "IN_PROGRESS" -> "REVIEW";
            case "REVIEW" -> "DONE";
            default -> null;
        };
    }

    public String getPrevStatus() {
        return switch (status) {
            case "IN_PROGRESS" -> "TODO";
            case "REVIEW" -> "IN_PROGRESS";
            case "DONE" -> "REVIEW";
            default -> null;
        };
    }
}
