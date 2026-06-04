package com.taskforge.ui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskforge.ui.model.ApiResponse;
import com.taskforge.ui.model.ProjectModel;
import com.taskforge.ui.service.ApiClient;
import com.taskforge.ui.session.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button newProjectButton;
    @FXML private FlowPane projectsPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();
        userNameLabel.setText(session.getCurrentUser().getName());
        userRoleLabel.setText(session.getCurrentUser().getRole());

        boolean isKetua = session.isKetua();
        newProjectButton.setVisible(isKetua);
        newProjectButton.setManaged(isKetua);

        loadProjects();
    }

    private void loadProjects() {
        loadingIndicator.setVisible(true);
        statusLabel.setText("");

        Task<List<ProjectModel>> fetchTask = new Task<>() {
            @Override
            protected List<ProjectModel> call() throws Exception {
                ApiResponse<Object> raw = ApiClient.get("/api/projects", Object.class);
                if (!raw.isSuccess()) throw new Exception(raw.getMessage());
                String json = MAPPER.writeValueAsString(raw.getData());
                return MAPPER.readValue(json, new TypeReference<List<ProjectModel>>() {});
            }
        };

        fetchTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            renderProjects(fetchTask.getValue());
        });

        fetchTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            statusLabel.setText("Gagal memuat proyek: " + fetchTask.getException().getMessage());
        });

        Thread t = new Thread(fetchTask);
        t.setDaemon(true);
        t.start();
    }

    private void renderProjects(List<ProjectModel> projects) {
        projectsPane.getChildren().clear();
        if (projects.isEmpty()) {
            Label empty = new Label("Belum ada proyek. Buat proyek baru untuk memulai.");
            empty.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");
            projectsPane.getChildren().add(empty);
            return;
        }
        for (ProjectModel project : projects) {
            projectsPane.getChildren().add(buildProjectCard(project));
        }
    }

    private VBox buildProjectCard(ProjectModel project) {
        VBox card = new VBox(8);
        card.setPrefWidth(260);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 2); -fx-cursor: hand;");

        Label title = new Label(project.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-wrap-text: true;");
        title.setMaxWidth(228);

        String deadlineText = project.getDeadline() != null
                ? "Deadline: " + project.getDeadline().format(FMT)
                : "Deadline: tidak ditentukan";
        Label deadline = new Label(deadlineText);
        deadline.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Label tasks = new Label(project.getCompletedTaskCount() + "/" + project.getTaskCount() + " task selesai");
        tasks.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        Label members = new Label(project.getMemberCount() + " anggota");
        members.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        ProgressBar progress = new ProgressBar(project.getTaskCount() == 0 ? 0
                : (double) project.getCompletedTaskCount() / project.getTaskCount());
        progress.setMaxWidth(Double.MAX_VALUE);

        if (project.getOverdueTaskCount() > 0) {
            Label overdueLabel = new Label("⚠ " + project.getOverdueTaskCount() + " task overdue");
            overdueLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 11px; -fx-font-weight: bold;");
            card.getChildren().addAll(title, deadline, tasks, members, progress, overdueLabel);
        } else {
            card.getChildren().addAll(title, deadline, tasks, members, progress);
        }

        card.setOnMouseClicked(e -> openKanban(project));
        return card;
    }

    private void openKanban(ProjectModel project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/kanban.fxml"));
            Stage stage = (Stage) projectsPane.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            KanbanController controller = loader.getController();
            controller.initWithProject(project);
            stage.setScene(scene);
            stage.setTitle("TaskForge — " + project.getTitle());
        } catch (Exception e) {
            statusLabel.setText("Gagal membuka Kanban: " + e.getMessage());
        }
    }

    @FXML
    public void handleNewProject() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Buat Proyek Baru");
        dialog.setHeaderText("Isi detail proyek");

        TextField titleField = new TextField();
        titleField.setPromptText("Judul proyek");
        TextArea descField = new TextArea();
        descField.setPromptText("Deskripsi (opsional)");
        descField.setPrefRowCount(3);

        VBox content = new VBox(10, new Label("Judul:"), titleField,
                new Label("Deskripsi:"), descField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && !titleField.getText().isBlank()) {
                submitNewProject(titleField.getText().trim(), descField.getText().trim());
            }
        });
    }

    private void submitNewProject(String title, String description) {
        Task<Void> createTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                var body = new java.util.HashMap<String, Object>();
                body.put("title", title);
                body.put("description", description.isEmpty() ? null : description);
                ApiClient.post("/api/projects", body, Object.class);
                return null;
            }
        };
        createTask.setOnSucceeded(e -> loadProjects());
        createTask.setOnFailed(e -> Platform.runLater(
                () -> statusLabel.setText("Gagal membuat proyek: " + createTask.getException().getMessage())));

        Thread t = new Thread(createTask);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 420, 480);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("TaskForge — Login");
        } catch (Exception e) {
            statusLabel.setText("Gagal logout");
        }
    }
}
