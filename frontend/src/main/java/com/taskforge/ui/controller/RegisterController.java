package com.taskforge.ui.controller;

import com.taskforge.ui.model.ApiResponse;
import com.taskforge.ui.model.UserModel;
import com.taskforge.ui.service.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import com.taskforge.ui.util.SceneNavigator;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Map;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleBox;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        roleBox.getItems().addAll("ANGGOTA", "KETUA");
        roleBox.setValue("ANGGOTA");
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
    }

    @FXML
    public void handleRegister() {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        String role     = roleBox.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Semua field wajib diisi");
            return;
        }
        if (password.length() < 8) {
            showError("Password minimal 8 karakter");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Password dan konfirmasi tidak cocok");
            return;
        }

        setLoading(true);

        Task<ApiResponse<UserModel>> task = new Task<>() {
            @Override
            protected ApiResponse<UserModel> call() throws Exception {
                Map<String, String> body = Map.of(
                        "name", name,
                        "email", email,
                        "password", password,
                        "role", role);
                return ApiClient.post("/api/auth/register", body, UserModel.class);
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            ApiResponse<UserModel> response = task.getValue();
            if (response.isSuccess()) {
                showSuccessAndGoLogin();
            } else {
                showError(response.getMessage() != null ? response.getMessage() : "Registrasi gagal");
            }
        });

        task.setOnFailed(e -> {
            setLoading(false);
            showError("Tidak dapat terhubung ke server. Pastikan backend berjalan.");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void showSuccessAndGoLogin() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Akun berhasil dibuat! Silakan login.", ButtonType.OK);
            alert.setHeaderText("Registrasi Berhasil");
            alert.showAndWait();
            goToLogin();
        });
    }

    @FXML
    public void handleBackToLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            Stage stage = (Stage) registerButton.getScene().getWindow();
            SceneNavigator.navigate(stage, "/fxml/login.fxml", "TaskForge — Login", 480, 660, false);
            stage.setResizable(false);
        } catch (Exception ex) {
            showError("Gagal kembali ke halaman login");
        }
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        });
    }

    private void setLoading(boolean loading) {
        Platform.runLater(() -> {
            registerButton.setDisable(loading);
            loadingIndicator.setVisible(loading);
            if (loading) errorLabel.setVisible(false);
        });
    }
}
