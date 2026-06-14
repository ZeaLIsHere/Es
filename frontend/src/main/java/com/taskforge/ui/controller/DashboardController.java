package com.taskforge.ui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskforge.ui.model.ApiResponse;
import com.taskforge.ui.model.ProjectModel;
import com.taskforge.ui.service.ApiClient;
import com.taskforge.ui.util.SceneNavigator;
import com.taskforge.ui.util.SidebarProfileBinder;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userNimLabel;
    @FXML private Label userRoleLabel;
    @FXML private FlowPane projectsPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private Label statTotalProyek;
    @FXML private Label statOverdue;
    @FXML private Label statSelesai;
    @FXML private TextField searchField;
    @FXML private Label avatarInitials;

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final String[] ACCENT_COLORS = {
        "#4F46E5", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6"
    };
    private static final String[] ACCENT_LIGHT = {
        "#EEF2FF", "#ECFDF5", "#FFFBEB", "#FEF2F2", "#F5F3FF"
    };

    private List<ProjectModel> allProjects = List.of();

    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);
        statusLabel.setText("");
        refreshSidebarProfile();
        loadProjects();
    }

    // ─── Sidebar / Profile ───────────────────────────────────────────────────

    private void refreshSidebarProfile() {
        SidebarProfileBinder.refresh(
                userNameLabel, userNimLabel, userRoleLabel, avatarInitials
        );
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
    public void handleNotifikasi() {
        navigate("/fxml/notifikasi.fxml", "TaskForge — Notifikasi");
    }

    // ─── Projects ────────────────────────────────────────────────────────────

    private void loadProjects() {
        loadingIndicator.setVisible(true);
        statusLabel.setText("");

        Task<List<ProjectModel>> fetchTask = new Task<>() {
            @Override
            protected List<ProjectModel> call() throws Exception {
                ApiResponse<Object> raw = ApiClient.get("/api/projects", Object.class);
                if (!raw.isSuccess()) throw new Exception(raw.getMessage());
                return MAPPER.convertValue(raw.getData(), new TypeReference<List<ProjectModel>>() {});
            }
        };

        fetchTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            allProjects = fetchTask.getValue();
            updateStatCards(allProjects);
            renderProjects(allProjects);
        });

        fetchTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            statusLabel.setText("Gagal memuat proyek: " + fetchTask.getException().getMessage());
        });

        Thread t = new Thread(fetchTask);
        t.setDaemon(true);
        t.start();
    }

    private void updateStatCards(List<ProjectModel> projects) {
        int totalProyek = projects.size();
        int totalOverdue = projects.stream().mapToInt(ProjectModel::getOverdueTaskCount).sum();
        int totalSelesai = projects.stream().mapToInt(ProjectModel::getCompletedTaskCount).sum();
        statTotalProyek.setText(String.valueOf(totalProyek));
        statOverdue.setText(String.valueOf(totalOverdue));
        statSelesai.setText(String.valueOf(totalSelesai));
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            renderProjects(allProjects);
        } else {
            List<ProjectModel> filtered = allProjects.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(query))
                .collect(Collectors.toList());
            renderProjects(filtered);
        }
    }

    private void renderProjects(List<ProjectModel> projects) {
        projectsPane.getChildren().clear();
        if (projects.isEmpty()) {
            Label empty = new Label("Belum ada proyek yang ditemukan.");
            empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
            projectsPane.getChildren().add(empty);
            return;
        }
        for (int i = 0; i < projects.size(); i++) {
            projectsPane.getChildren().add(buildProjectCard(projects.get(i), i));
        }
    }

    private VBox buildProjectCard(ProjectModel project, int index) {
        String accentColor = ACCENT_COLORS[index % ACCENT_COLORS.length];
        String accentLight = ACCENT_LIGHT[index % ACCENT_LIGHT.length];

        VBox card = new VBox(0);
        card.setPrefWidth(270);
        card.setMaxWidth(270);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 3); -fx-cursor: hand;"
        );

        HBox accentBar = new HBox();
        accentBar.setPrefHeight(6);
        accentBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 12px 12px 0 0;");

        VBox body = new VBox(8);
        body.setPadding(new Insets(14, 16, 14, 16));

        Label title = new Label(project.getTitle());
        title.setWrapText(true);
        title.setMaxWidth(238);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        String deadlineText = project.getDeadline() != null
                ? "  " + project.getDeadline().format(FMT)
                : "  Belum ada deadline";
        Label deadline = new Label(deadlineText);
        deadline.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label members = new Label("  " + project.getMemberCount() + " anggota");
        members.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        int taskCount = project.getTaskCount();
        int completedCount = project.getCompletedTaskCount();
        double progressVal = taskCount == 0 ? 0 : (double) completedCount / taskCount;
        int progressPct = (int) Math.round(progressVal * 100);

        HBox progressHeader = new HBox();
        progressHeader.setAlignment(Pos.CENTER_LEFT);
        Label tasksLabel = new Label(completedCount + "/" + taskCount + " task selesai");
        tasksLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label pctLabel = new Label(progressPct + "%");
        pctLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        progressHeader.getChildren().addAll(tasksLabel, spacer, pctLabel);

        ProgressBar progressBar = new ProgressBar(progressVal);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(7);
        progressBar.setStyle("-fx-accent: " + accentColor + ";");

        body.getChildren().addAll(title, deadline, members, progressHeader, progressBar);

        if (project.getOverdueTaskCount() > 0) {
            Label overdueLabel = new Label("  " + project.getOverdueTaskCount() + " task overdue");
            overdueLabel.setStyle(
                "-fx-text-fill: #DC2626; -fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-background-color: #FEF2F2; -fx-background-radius: 4px; -fx-padding: 3 8 3 8;"
            );
            body.getChildren().add(overdueLabel);
        }

        card.getChildren().addAll(accentBar, body);
        card.setOnMouseClicked(e -> openProjectDetail(project));

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + accentLight + "; -fx-background-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0, 0, 5); -fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 3); -fx-cursor: hand;"
        ));

        return card;
    }

    private void openProjectDetail(ProjectModel project) {
        try {
            Stage stage = (Stage) projectsPane.getScene().getWindow();
            ProjectDetailController controller = SceneNavigator.navigate(
                    stage, "/fxml/project-detail.fxml",
                    "TaskForge — " + project.getTitle(), 1100, 700);
            controller.initWithProject(project);
        } catch (Exception e) {
            statusLabel.setText("Gagal membuka detail proyek: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        SidebarProfileBinder.logout(userNameLabel);
    }

    private void navigate(String fxml, String title) {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            SceneNavigator.navigate(stage, fxml, title, 1100, 700);
        } catch (Exception e) {
            statusLabel.setText("Gagal navigasi: " + e.getMessage());
        }
    }
}
