package com.taskforge.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoreModel {

    private Long userId;
    private String userName;
    private String userEmail;
    private double score;
    private int tasksCompleted;
    private int tasksOntime;
    private double percentage;
    private String scoreLevel; // "HIGH" | "MEDIUM" | "LOW"

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(int tasksCompleted) { this.tasksCompleted = tasksCompleted; }

    public int getTasksOntime() { return tasksOntime; }
    public void setTasksOntime(int tasksOntime) { this.tasksOntime = tasksOntime; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getScoreLevel() { return scoreLevel; }
    public void setScoreLevel(String scoreLevel) { this.scoreLevel = scoreLevel; }

    // Returns CSS style class for the progress bar
    public String getBarStyleClass() {
        return switch (scoreLevel != null ? scoreLevel : "") {
            case "HIGH"   -> "score-bar-good";
            case "MEDIUM" -> "score-bar-ok";
            default       -> "score-bar-low";
        };
    }
}
