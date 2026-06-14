package com.taskforge.ui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskforge.ui.model.ApiResponse;
import com.taskforge.ui.model.FileModel;
import com.taskforge.ui.model.TaskCommentModel;
import com.taskforge.ui.model.TaskModel;
import com.taskforge.ui.model.UserModel;
import com.taskforge.ui.service.ApiClient;
import com.taskforge.ui.session.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TaskDetailModalController {

    @FXML private Label titleLabel;
    @FXML private Label descLabel;
    @FXML private Label statusLabel;
    @FXML private Label priorityLabel;
    @FXML private Label assigneeLabel;
    @FXML private Label deadlineLabel;

    @FXML private Label fileCountLabel;
    @FXML private VBox fileListContainer;
    @FXML private Label noFileLabel;
    @FXML private Button uploadFileBtn;

    @FXML private Label commentCountLabel;
    @FXML private VBox commentListContainer;
    @FXML private TextField commentField;

    @FXML private HBox actionButtonsContainer;
    @FXML private Label statusInfoLabel;

    private TaskModel currentTask;
    private KanbanController kanbanController;
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public void initWithTask(TaskModel task, KanbanController kanbanController) {
        this.currentTask = task;
        this.kanbanController = kanbanController;

        titleLabel.setText(task.getTitle());
        descLabel.setText(task.getDescription() != null ? task.getDescription() : "-");
        statusLabel.setText(task.getStatus());
        priorityLabel.setText(task.getPriority());
        assigneeLabel.setText(task.getAssigneeName());
        deadlineLabel.setText(task.getDeadlineLabel());

        boolean isKetua = SessionManager.getInstance().isKetua();
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(SessionManager.getInstance().getCurrentUser().getId());

        uploadFileBtn.setVisible(isKetua || isAssignee);
        uploadFileBtn.setManaged(isKetua || isAssignee);

        loadFiles();
        loadComments();
        renderActionButtons(isKetua, isAssignee);
    }

    private void renderActionButtons(boolean isKetua, boolean isAssignee) {
        actionButtonsContainer.getChildren().removeIf(node -> node instanceof Button);
        statusInfoLabel.setText("");

        if (isKetua || isAssignee) {
            String status = currentTask.getStatus();
            if ("TODO".equals(status)) {
                addButton("Mulai Dikerjakan", "IN_PROGRESS", "#4F46E5");
            } else if ("IN_PROGRESS".equals(status)) {
                addButton("Ajukan Review", "REVIEW", "#D97706");
            }
        }

        if (isKetua && "REVIEW".equals(currentTask.getStatus())) {
            addButton("Minta Revisi", "IN_PROGRESS", "#DC2626");
            addButton("Approve Task", "DONE", "#059669");
        }
    }

    private void addButton(String text, String targetStatus, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnAction(e -> updateStatus(targetStatus));
        actionButtonsContainer.getChildren().add(btn);
    }

    private void updateStatus(String newStatus) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Map<String, String> body = Map.of("status", newStatus);
                ApiResponse<Object> response = ApiClient.put("/api/tasks/" + currentTask.getId() + "/status", body, Object.class);
                if (!response.isSuccess()) throw new Exception(response.getMessage());
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            kanbanController.refreshData();
            handleClose();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> statusInfoLabel.setText(task.getException().getMessage())));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void loadFiles() {
        Task<List<FileModel>> task = new Task<>() {
            @Override
            protected List<FileModel> call() throws Exception {
                ApiResponse<Object> response = ApiClient.get("/api/tasks/" + currentTask.getId() + "/files", Object.class);
                if (!response.isSuccess()) throw new Exception(response.getMessage());
                String json = MAPPER.writeValueAsString(response.getData());
                return MAPPER.readValue(json, new TypeReference<>() {});
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> renderFiles(task.getValue())));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void renderFiles(List<FileModel> files) {
        fileListContainer.getChildren().clear();
        fileCountLabel.setText("FILE PROGRESS (" + files.size() + ")");

        if (files.isEmpty()) {
            fileListContainer.getChildren().add(noFileLabel);
            return;
        }

        boolean isKetua = SessionManager.getInstance().isKetua();
        UserModel me = SessionManager.getInstance().getCurrentUser();

        for (FileModel f : files) {
            HBox fileRow = new HBox(6);
            fileRow.setAlignment(Pos.CENTER_LEFT);

            Label fLabel = new Label("📎 " + f.getName() + " (" + f.getUploaderName() + ")");
            fLabel.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");
            fileRow.getChildren().add(fLabel);

            boolean isMyFile = f.getUploadedBy() != null && f.getUploadedBy().getId().equals(me.getId());
            if (isKetua || isMyFile) {
                Button delBtn = new Button("✕");
                delBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-padding: 1 5; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                delBtn.setOnAction(e -> deleteFile(f.getId()));
                fileRow.getChildren().add(delBtn);
            }

            fileListContainer.getChildren().add(fileRow);
        }
    }

    private void deleteFile(Long fileId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiResponse<Object> response = ApiClient.delete("/api/files/" + fileId, Object.class);
                if (!response.isSuccess()) throw new Exception(response.getMessage());
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(this::loadFiles));
        task.setOnFailed(e -> Platform.runLater(() -> statusInfoLabel.setText("Gagal hapus file: " + task.getException().getMessage())));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void handleUploadFile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tugas Files", "*.zip", "*.rar", "*.pdf", "*.ppt", "*.pptx", "*.doc", "*.docx", "*.png", "*.jpg", "*.jpeg", "*.txt"));
        File file = chooser.showOpenDialog(titleLabel.getScene().getWindow());
        if (file == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiResponse<Object> response = ApiClient.postMultipart("/api/tasks/" + currentTask.getId() + "/files/upload", file, Object.class);
                if (!response.isSuccess()) throw new Exception(response.getMessage());
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(this::loadFiles));
        task.setOnFailed(e -> Platform.runLater(() -> statusInfoLabel.setText("Gagal upload: " + task.getException().getMessage())));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void loadComments() {
        Task<List<TaskCommentModel>> task = new Task<>() {
            @Override
            protected List<TaskCommentModel> call() throws Exception {
                ApiResponse<Object> response = ApiClient.get("/api/tasks/" + currentTask.getId() + "/comments", Object.class);
                if (!response.isSuccess()) throw new Exception(response.getMessage());
                String json = MAPPER.writeValueAsString(response.getData());
                return MAPPER.readValue(json, new TypeReference<>() {});
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> renderComments(task.getValue())));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void renderComments(List<TaskCommentModel> comments) {
        commentListContainer.getChildren().clear();
        commentCountLabel.setText("KOMENTAR (" + comments.size() + ")");

        for (TaskCommentModel c : comments) {
            VBox box = new VBox(2);
            box.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 8 12; -fx-background-radius: 8;");
            
            HBox header = new HBox(6);
            header.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(c.getUserName());
            name.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-font-size: 11px;");
            Label role = new Label("[" + c.getUserRole() + "]");
            role.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10px;");
            header.getChildren().addAll(name, role);

            Label content = new Label(c.getContent());
            content.setWrapText(true);
            content.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");

            box.getChildren().addAll(header, content);
            commentListContainer.getChildren().add(box);
        }
    }

    @FXML
    public void handleSendComment() {
        String text = commentField.getText().trim();
        if (text.isEmpty()) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Map<String, String> body = Map.of("content", text);
                ApiResponse<Object> response = ApiClient.post("/api/tasks/" + currentTask.getId() + "/comments", body, Object.class);
                if (!response.isSuccess()) throw new Exception(response.getMessage());
                return null;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            commentField.clear();
            loadComments();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> statusInfoLabel.setText("Gagal kirim komentar: " + task.getException().getMessage())));
        
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
