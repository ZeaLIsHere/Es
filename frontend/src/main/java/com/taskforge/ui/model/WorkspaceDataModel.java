package com.taskforge.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkspaceDataModel {

    private List<WorkspaceNotificationModel> notifications;
    private int totalCount;
    private int unreadCount;

    public List<WorkspaceNotificationModel> getNotifications() { return notifications; }
    public void setNotifications(List<WorkspaceNotificationModel> notifications) {
        this.notifications = notifications;
    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
