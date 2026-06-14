package com.taskforge.ui.util;

import com.taskforge.ui.model.UserModel;
import com.taskforge.ui.session.SessionManager;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public final class SidebarProfileBinder {

    private SidebarProfileBinder() {}

    public static void refresh(Label userNameLabel, Label userNimLabel, Label userRoleLabel,
                               Label avatarInitials) {
        UserModel user = SessionManager.getInstance().getCurrentUser();
        userNameLabel.setText(user.getName());
        userRoleLabel.setText(user.getRole());

        if (user.getNim() != null && !user.getNim().isBlank()) {
            userNimLabel.setText("NIM: " + user.getNim());
            userNimLabel.setVisible(true);
            userNimLabel.setManaged(true);
        } else {
            userNimLabel.setText("");
            userNimLabel.setVisible(false);
            userNimLabel.setManaged(false);
        }

        avatarInitials.setText(getInitials(user.getName()));
    }

    public static void logout(Label anchor) {
        SessionManager.getInstance().clearSession();
        try {
            Stage stage = (Stage) anchor.getScene().getWindow();
            SceneNavigator.navigate(stage, "/fxml/login.fxml", "TaskForge — Login", 480, 660, false);
            stage.setResizable(false);
        } catch (Exception e) {
            if (anchor != null) anchor.setText("Gagal logout");
        }
    }

    public static String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
