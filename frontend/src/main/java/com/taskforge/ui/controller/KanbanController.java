package com.taskforge.ui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskforge.ui.model.ApiResponse;
import com.taskforge.ui.model.ProjectModel;
import com.taskforge.ui.model.TaskModel;
import com.taskforge.ui.model.UserModel;
import com.taskforge.ui.service.ApiClient;
import com.taskforge.ui.util.SceneNavigator;
import com.taskforge.ui.session.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    public void initWithProject(ProjectModel project) {
        this.currentProject = project;
        projectTitleLabel.setText(project.getTitle());

        boolean isKetua = SessionManager.getInstance().isKetua();
        addTaskButton.setVisible(isKetua);
        addTaskButton.setManaged(isKetua);

        setupDragAndDrop();
        loadTasks();
    }

    public void refreshData() {
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

        // Overdue styling
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

        card.getChildren().addAll(titleRow, assigneeLabel, deadlineLabel);

        // Click to open Detail Modal
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                showTaskDetailModal(task);
            }
        });

        // Drag and Drop
        boolean isKetua = SessionManager.getInstance().isKetua();
        boolean isMyTask = isMyTask(task);
        boolean canMove = isKetua || isMyTask;

        if (canMove) {
            card.setOnDragDetected(e -> {
                Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(task.getId().toString() + ":" + task.getStatus());
                db.setContent(content);
                e.consume();
            });
            card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
        }

        return card;
    }

    private void setupDragAndDrop() {
        setupColumnDropTarget(todoColumn, "TODO");
        setupColumnDropTarget(inProgressColumn, "IN_PROGRESS");
        setupColumnDropTarget(reviewColumn, "REVIEW");
        setupColumnDropTarget(doneColumn, "DONE");
    }

    private void setupColumnDropTarget(VBox column, String status) {
        column.setOnDragOver(e -> {
            if (e.getGestureSource() != column && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        column.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String[] parts = db.getString().split(":");
                Long taskId = Long.parseLong(parts[0]);
                String oldStatus = parts[1];
                if (!oldStatus.equals(status)) {
                    TaskModel t = new TaskModel();
                    t.setId(taskId);
                    moveTask(t, status);
                }
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private void showTaskDetailModal(TaskModel task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/task-detail-modal.fxml"));
            javafx.scene.Parent root = loader.load();
            TaskDetailModalController controller = loader.getController();
            controller.initWithTask(task, this);

            Stage stage = new Stage();
            stage.setTitle("Detail Task - " + task.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 600, 700));
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("Gagal membuka detail task: " + e.getMessage());
            e.printStackTrace();
        }
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
        updateTask.setOnFailed(e -> Platform.runLater(() -> {
            statusLabel.setText("Gagal memindahkan: " + updateTask.getException().getMessage());
            // Show alert for the validation exception (e.g. missing file)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Gagal Memindahkan Task");
            alert.setHeaderText(null);
            alert.setContentText(updateTask.getException().getMessage());
            alert.showAndWait();
            loadTasks(); // refresh to snap back
        }));

        Thread t = new Thread(updateTask);
        t.setDaemon(true);
        t.start();
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
            Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
            FileHubController ctrl = SceneNavigator.navigate(
                    stage, "/fxml/filehub.fxml", "TaskForge — File Hub", 1200, 700);
            ctrl.initWithProject(currentProject);
        } catch (Exception e) {
            statusLabel.setText("Gagal membuka File Hub: " + e.getMessage());
        }
    }

    @FXML
    public void handleReport() {
        try {
            Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
            ReportScoreController ctrl = SceneNavigator.navigate(
                    stage, "/fxml/report.fxml", "TaskForge — Kontribusi & Laporan", 1000, 680);
            ctrl.initWithProject(currentProject);
        } catch (Exception e) {
            statusLabel.setText("Gagal membuka Laporan: " + e.getMessage());
        }
    }

    @FXML
    public void handleBack() {
        if (currentProject == null) {
            navigateToDashboard();
            return;
        }
        try {
            Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
            ProjectDetailController controller = SceneNavigator.navigate(
                    stage, "/fxml/project-detail.fxml",
                    "TaskForge — " + currentProject.getTitle(), 1100, 700);
            controller.initWithProject(currentProject);
        } catch (Exception e) {
            statusLabel.setText("Gagal kembali ke detail proyek");
        }
    }

    private void navigateToDashboard() {
        try {
            Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
            SceneNavigator.navigate(stage, "/fxml/dashboard.fxml", "TaskForge — Dashboard", 1100, 700);
        } catch (Exception e) {
            statusLabel.setText("Gagal kembali ke dashboard");
        }
    }
}
