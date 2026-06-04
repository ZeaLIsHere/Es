package com.taskforge.ui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskforge.ui.model.ApiResponse;
import com.taskforge.ui.model.ProjectModel;
import com.taskforge.ui.model.TaskModel;
import com.taskforge.ui.model.UserModel;
import com.taskforge.ui.service.ApiClient;
import com.taskforge.ui.session.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KanbanController {

    @FXML private Label projectTitleLabel;
    @FXML private Button addTaskButton;
    @FXML private Label statusLabel;
    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox reviewColumn;
    @FXML private VBox doneColumn;

    private ProjectModel currentProject;
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public void initWithProject(ProjectModel project) {
        this.currentProject = project;
        projectTitleLabel.setText(project.getTitle());

        boolean isKetua = SessionManager.getInstance().isKetua();
        addTaskButton.setVisible(isKetua);
        addTaskButton.setManaged(isKetua);

        loadTasks();
    }

    private void loadTasks() {
        clearColumns();
        statusLabel.setText("Memuat tasks...");

        Task<List<TaskModel>> fetchTask = new Task<>() {
            @Override
            protected List<TaskModel> call() throws Exception {
                ApiResponse<Object> raw = ApiClient.get(
                        "/api/projects/" + currentProject.getId() + "/tasks", Object.class);
                if (!raw.isSuccess()) throw new Exception(raw.getMessage());
                String json = MAPPER.writeValueAsString(raw.getData());
                return MAPPER.readValue(json, new TypeReference<List<TaskModel>>() {});
            }
        };

        fetchTask.setOnSucceeded(e -> {
            statusLabel.setText("");
            renderKanban(fetchTask.getValue());
        });

        fetchTask.setOnFailed(e -> Platform.runLater(
                () -> statusLabel.setText("Gagal memuat tasks: " + fetchTask.getException().getMessage())));

        Thread t = new Thread(fetchTask);
        t.setDaemon(true);
        t.start();
    }

    private void clearColumns() {
        // Keep the column header (first child), remove task cards
        trimColumn(todoColumn);
        trimColumn(inProgressColumn);
        trimColumn(reviewColumn);
        trimColumn(doneColumn);
    }

    private void trimColumn(VBox col) {
        if (col.getChildren().size() > 1) {
            col.getChildren().subList(1, col.getChildren().size()).clear();
        }
    }

    private void renderKanban(List<TaskModel> tasks) {
        Map<String, List<TaskModel>> byStatus = tasks.stream()
                .collect(Collectors.groupingBy(TaskModel::getStatus));

        addTaskCards(todoColumn, byStatus.getOrDefault("TODO", List.of()));
        addTaskCards(inProgressColumn, byStatus.getOrDefault("IN_PROGRESS", List.of()));
        addTaskCards(reviewColumn, byStatus.getOrDefault("REVIEW", List.of()));
        addTaskCards(doneColumn, byStatus.getOrDefault("DONE", List.of()));
    }

    private void addTaskCards(VBox column, List<TaskModel> tasks) {
        for (TaskModel task : tasks) {
            column.getChildren().add(buildTaskCard(task));
        }
    }

    private VBox buildTaskCard(TaskModel task) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setMaxWidth(Double.MAX_VALUE);

        // Overdue styling (red card background)
        if (task.isOverdue()) {
            card.getStyleClass().addAll("task-card", "task-card-overdue");
        } else {
            card.getStyleClass().add("task-card");
        }

        // Priority badge + Title row
        HBox titleRow = new HBox(6);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label priorityBadge = new Label(task.getPriority());
        priorityBadge.getStyleClass().add(switch (task.getPriority()) {
            case "HIGH" -> "badge-high";
            case "MEDIUM" -> "badge-medium";
            default -> "badge-low";
        });
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(170);
        titleRow.getChildren().addAll(priorityBadge, titleLabel);

        // Assignee
        Label assigneeLabel = new Label("👤 " + task.getAssigneeName());
        assigneeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

        // Deadline
        String deadlineStyle = task.isOverdue()
                ? "-fx-font-size: 11px; -fx-text-fill: #DC2626; -fx-font-weight: bold;"
                : "-fx-font-size: 11px; -fx-text-fill: #6B7280;";
        Label deadlineLabel = new Label("📅 " + task.getDeadlineLabel());
        deadlineLabel.setStyle(deadlineStyle);

        // Move buttons
        HBox controls = new HBox(4);
        controls.setAlignment(Pos.CENTER_RIGHT);
        controls.setMaxWidth(Double.MAX_VALUE);

        boolean isKetua = SessionManager.getInstance().isKetua();
        boolean isMyTask = isMyTask(task);
        boolean canMove = isKetua || isMyTask;

        if (canMove) {
            String prevStatus = task.getPrevStatus();
            String nextStatus = task.getNextStatus();

            if (prevStatus != null && isKetua) {
                Button prevBtn = new Button("←");
                prevBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2 6 2 6; -fx-background-color: #E5E7EB; -fx-background-radius: 4px; -fx-cursor: hand;");
                prevBtn.setOnAction(e -> moveTask(task, prevStatus));
                controls.getChildren().add(prevBtn);
            }
            if (nextStatus != null) {
                Button nextBtn = new Button("→");
                nextBtn.setStyle("-fx-font-size: 11px; -fx-padding: 2 6 2 6; -fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 4px; -fx-cursor: hand;");
                nextBtn.setOnAction(e -> moveTask(task, nextStatus));
                controls.getChildren().add(nextBtn);
            }
        }

        // Delete button (KETUA only)
        if (isKetua) {
            Button deleteBtn = new Button("✕");
            deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 5 2 5; -fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 4px; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> deleteTask(task));
            controls.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(titleRow, assigneeLabel, deadlineLabel);
        if (!controls.getChildren().isEmpty()) {
            card.getChildren().add(controls);
        }

        return card;
    }

    private boolean isMyTask(TaskModel task) {
        UserModel me = SessionManager.getInstance().getCurrentUser();
        return task.getAssignee() != null && task.getAssignee().getId().equals(me.getId());
    }

    private void moveTask(TaskModel task, String newStatus) {
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Map<String, String> body = Map.of("status", newStatus);
                ApiResponse<Object> response = ApiClient.put(
                        "/api/tasks/" + task.getId() + "/status", body, Object.class);
                if (!response.isSuccess()) throw new Exception(response.getMessage());
                return null;
            }
        };

        updateTask.setOnSucceeded(e -> Platform.runLater(this::loadTasks));
        updateTask.setOnFailed(e -> Platform.runLater(
                () -> statusLabel.setText("Gagal memindahkan task: " + updateTask.getException().getMessage())));

        Thread t = new Thread(updateTask);
        t.setDaemon(true);
        t.start();
    }

    private void deleteTask(TaskModel task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus task '" + task.getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Konfirmasi Hapus");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.YES) {
                Task<Void> deleteApiTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ApiClient.delete("/api/tasks/" + task.getId(), Object.class);
                        return null;
                    }
                };
                deleteApiTask.setOnSucceeded(e -> Platform.runLater(this::loadTasks));
                deleteApiTask.setOnFailed(e -> Platform.runLater(
                        () -> statusLabel.setText("Gagal menghapus task")));
                Thread t = new Thread(deleteApiTask);
                t.setDaemon(true);
                t.start();
            }
        });
    }

    @FXML
    public void handleAddTask() {
        if (!SessionManager.getInstance().isKetua()) return;

        // Load project members for assignee dropdown
        Task<List<UserModel>> loadMembers = new Task<>() {
            @Override
            protected List<UserModel> call() throws Exception {
                ApiResponse<Object> raw = ApiClient.get(
                        "/api/projects/" + currentProject.getId(), Object.class);
                String json = MAPPER.writeValueAsString(raw.getData());
                ProjectModel detail = MAPPER.readValue(json, ProjectModel.class);
                // combine owner + members
                List<UserModel> all = new java.util.ArrayList<>();
                if (detail.getOwner() != null) all.add(detail.getOwner());
                if (detail.getMembers() != null) all.addAll(detail.getMembers());
                return all;
            }
        };

        loadMembers.setOnSucceeded(e -> Platform.runLater(
                () -> showCreateTaskDialog(loadMembers.getValue())));
        loadMembers.setOnFailed(e -> Platform.runLater(
                () -> showCreateTaskDialog(List.of())));

        Thread t = new Thread(loadMembers);
        t.setDaemon(true);
        t.start();
    }

    private void showCreateTaskDialog(List<UserModel> members) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Buat Task Baru");
        dialog.setHeaderText("Isi detail task");

        TextField titleField = new TextField();
        titleField.setPromptText("Judul task");

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("LOW", "MEDIUM", "HIGH");
        priorityBox.setValue("MEDIUM");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("SIMPLE", "MILESTONE");
        typeBox.setValue("SIMPLE");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setValue(java.time.LocalDate.now().plusDays(7));

        ComboBox<UserModel> assigneeBox = new ComboBox<>();
        assigneeBox.getItems().addAll(members);
        assigneeBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(UserModel u) { return u == null ? "" : u.getName() + " (" + u.getRole() + ")"; }
            @Override public UserModel fromString(String s) { return null; }
        });
        if (!members.isEmpty()) assigneeBox.setValue(members.get(0));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Judul:"), titleField);
        grid.addRow(1, new Label("Prioritas:"), priorityBox);
        grid.addRow(2, new Label("Tipe:"), typeBox);
        grid.addRow(3, new Label("Deadline:"), deadlinePicker);
        grid.addRow(4, new Label("Assignee:"), assigneeBox);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK
                    && !titleField.getText().isBlank()
                    && assigneeBox.getValue() != null
                    && deadlinePicker.getValue() != null) {

                Map<String, Object> body = new java.util.HashMap<>();
                body.put("title", titleField.getText().trim());
                body.put("priority", priorityBox.getValue());
                body.put("taskType", typeBox.getValue());
                body.put("assigneeId", assigneeBox.getValue().getId());
                body.put("deadline", deadlinePicker.getValue().atTime(23, 59, 0).toString());

                Task<Void> createTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        ApiResponse<Object> response = ApiClient.post(
                                "/api/projects/" + currentProject.getId() + "/tasks", body, Object.class);
                        if (!response.isSuccess()) throw new Exception(response.getMessage());
                        return null;
                    }
                };
                createTask.setOnSucceeded(e -> Platform.runLater(this::loadTasks));
                createTask.setOnFailed(e -> Platform.runLater(
                        () -> statusLabel.setText("Gagal: " + createTask.getException().getMessage())));
                Thread t = new Thread(createTask);
                t.setDaemon(true);
                t.start();
            }
        });
    }

    @FXML
    public void handleFileHub() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/filehub.fxml"));
            Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            FileHubController ctrl = loader.getController();
            ctrl.initWithProject(currentProject);
            stage.setScene(scene);
            stage.setTitle("TaskForge — File Hub");
        } catch (Exception e) {
            statusLabel.setText("Gagal membuka File Hub: " + e.getMessage());
        }
    }

    @FXML
    public void handleReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/report.fxml"));
            Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1000, 680);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            ReportScoreController ctrl = loader.getController();
            ctrl.initWithProject(currentProject);
            stage.setScene(scene);
            stage.setTitle("TaskForge — Kontribusi & Laporan");
        } catch (Exception e) {
            statusLabel.setText("Gagal membuka Laporan: " + e.getMessage());
        }
    }

    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("TaskForge — Dashboard");
        } catch (Exception e) {
            statusLabel.setText("Gagal kembali ke dashboard");
        }
    }
}
