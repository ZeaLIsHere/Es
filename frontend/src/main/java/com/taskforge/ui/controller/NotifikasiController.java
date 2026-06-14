package com.taskforge.ui.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskforge.ui.model.ApiResponse;
import com.taskforge.ui.model.WorkspaceDataModel;
import com.taskforge.ui.model.WorkspaceNotificationModel;
import com.taskforge.ui.service.ApiClient;
import com.taskforge.ui.util.SceneNavigator;
import com.taskforge.ui.util.SidebarProfileBinder;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotifikasiController {

    @FXML private Label userNameLabel;
    @FXML private Label userNimLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label avatarInitials;
    @FXML private Label subtitleLabel;
    @FXML private Label badgeCountLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private VBox emptyStatePane;
    @FXML private VBox notificationListPane;

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);
        statusLabel.setText("");
        refreshSidebarProfile();
        loadWorkspaceData();
    }

    private void refreshSidebarProfile() {
        SidebarProfileBinder.refresh(
                userNameLabel, userNimLabel, userRoleLabel, avatarInitials
        );
    }

    private void loadWorkspaceData() {
        loadingIndicator.setVisible(true);
        statusLabel.setText("");

        Task<WorkspaceDataModel> fetchTask = new Task<>() {
            @Override
            protected WorkspaceDataModel call() throws Exception {
                ApiResponse<Object> raw = ApiClient.get("/api/workspace/data", Object.class);
                if (!raw.isSuccess()) throw new Exception(raw.getMessage());
                return MAPPER.convertValue(raw.getData(), WorkspaceDataModel.class);
            }
        };

        fetchTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            renderWorkspaceData(fetchTask.getValue());
        });

        fetchTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            statusLabel.setText("Gagal memuat notifikasi: " + fetchTask.getException().getMessage());
            showEmptyState();
        });

        Thread t = new Thread(fetchTask);
        t.setDaemon(true);
        t.start();
    }

    private void renderWorkspaceData(WorkspaceDataModel data) {
        if (data == null || data.getNotifications() == null || data.getNotifications().isEmpty()) {
            badgeCountLabel.setVisible(false);
            badgeCountLabel.setManaged(false);
            showEmptyState();
            return;
        }

        List<WorkspaceNotificationModel> notifications = data.getNotifications();
        badgeCountLabel.setText(data.getTotalCount() + " aktivitas");
        badgeCountLabel.setVisible(true);
        badgeCountLabel.setManaged(true);
        subtitleLabel.setText("Menampilkan " + notifications.size() + " notifikasi terbaru");

        emptyStatePane.setVisible(false);
        emptyStatePane.setManaged(false);
        notificationListPane.setVisible(true);
        notificationListPane.setManaged(true);
        notificationListPane.getChildren().clear();

        for (WorkspaceNotificationModel notification : notifications) {
            notificationListPane.getChildren().add(buildNotificationCard(notification));
        }
    }

    private void showEmptyState() {
        emptyStatePane.setVisible(true);
        emptyStatePane.setManaged(true);
        notificationListPane.setVisible(false);
        notificationListPane.setManaged(false);
        notificationListPane.getChildren().clear();
    }

    private VBox buildNotificationCard(WorkspaceNotificationModel notification) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12px; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 12px; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 8, 0, 0, 2);"
        );
        card.setPadding(new Insets(16, 18, 16, 18));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getActionIcon(notification.getActionType()));
        icon.setStyle(
                "-fx-font-size: 14px; -fx-min-width: 36; -fx-min-height: 36; " +
                "-fx-alignment: center; -fx-background-color: #EEF2FF; " +
                "-fx-background-radius: 10; -fx-text-fill: #4F46E5;"
        );
        icon.setMaxWidth(36);
        icon.setMaxHeight(36);

        VBox titleBox = new VBox(2);
        Label projectLabel = new Label(notification.getProjectTitle());
        projectLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4F46E5;");
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(620);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1E293B;");
        titleBox.getChildren().addAll(projectLabel, messageLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String timeText = notification.getTimestamp() != null
                ? notification.getTimestamp().format(FMT)
                : "";
        Label timeLabel = new Label(timeText);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        header.getChildren().addAll(icon, titleBox, spacer, timeLabel);

        Label actorLabel = new Label("Oleh " + notification.getActorName());
        actorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-padding: 0 0 0 46;");

        card.getChildren().addAll(header, actorLabel);
        return card;
    }

    private String getActionIcon(String actionType) {
        if (actionType == null) return "•";
        return switch (actionType) {
            case "TASK_CREATED" -> "+";
            case "STATUS_CHANGED" -> "↻";
            case "FILE_UPLOADED" -> "↑";
            case "LINK_ADDED" -> "🔗";
            case "ASSIGNEE_CHANGED" -> "→";
            case "MEMBER_ADDED" -> "👤";
            default -> "•";
        };
    }

    @FXML
    public void handleProyek() {
        navigate("/fxml/proyek.fxml", "TaskForge — Proyek");
    }

    @FXML
    public void handleProfil() {
        navigate("/fxml/profil.fxml", "TaskForge — Profil");
    }

    @FXML
    public void handleDashboard() {
        navigate("/fxml/dashboard.fxml", "TaskForge — Dashboard");
    }

    @FXML
    public void handleLogout() {
        SidebarProfileBinder.logout(userNameLabel);
    }

    private void navigate(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            SceneNavigator.navigate(stage, fxmlPath, title, 1100, 700);
        } catch (Exception e) {
            statusLabel.setText("Gagal navigasi: " + e.getMessage());
        }
    }
}
