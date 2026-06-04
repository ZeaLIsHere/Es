package com.taskforge.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectModel {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private UserModel owner;
    private List<UserModel> members;
    private int taskCount;
    private int completedTaskCount;
    private int overdueTaskCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserModel getOwner() { return owner; }
    public void setOwner(UserModel owner) { this.owner = owner; }

    public List<UserModel> getMembers() { return members; }
    public void setMembers(List<UserModel> members) { this.members = members; }

    public int getTaskCount() { return taskCount; }
    public void setTaskCount(int taskCount) { this.taskCount = taskCount; }

    public int getCompletedTaskCount() { return completedTaskCount; }
    public void setCompletedTaskCount(int completedTaskCount) { this.completedTaskCount = completedTaskCount; }

    public int getOverdueTaskCount() { return overdueTaskCount; }
    public void setOverdueTaskCount(int overdueTaskCount) { this.overdueTaskCount = overdueTaskCount; }

    public int getMemberCount() {
        int count = members != null ? members.size() : 0;
        return count + 1; // +1 for owner
    }
}
