package com.taskforge.ui.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneNavigator {

    private static final String STYLESHEET = "/css/styles.css";

    private SceneNavigator() {}

    public static <T> T navigate(Stage stage, String fxmlPath, String title,
                                 double defaultWidth, double defaultHeight) throws Exception {
        return navigate(stage, fxmlPath, title, defaultWidth, defaultHeight, true);
    }

    public static <T> T navigate(Stage stage, String fxmlPath, String title,
                                 double defaultWidth, double defaultHeight,
                                 boolean preserveWindowState) throws Exception {
        boolean wasMaximized = stage.isMaximized();
        boolean wasFullScreen = stage.isFullScreen();
        double savedWidth = stage.getWidth();
        double savedHeight = stage.getHeight();

        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        applyStylesheet(scene);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(true);

        if (preserveWindowState) {
            Platform.runLater(() -> restoreWindowState(
                    stage, wasMaximized, wasFullScreen, savedWidth, savedHeight,
                    defaultWidth, defaultHeight));
        } else {
            Platform.runLater(() -> {
                stage.setFullScreen(false);
                stage.setMaximized(false);
                stage.setWidth(defaultWidth);
                stage.setHeight(defaultHeight);
            });
        }

        return loader.getController();
    }

    private static void restoreWindowState(Stage stage, boolean wasMaximized, boolean wasFullScreen,
                                           double savedWidth, double savedHeight,
                                           double defaultWidth, double defaultHeight) {
        if (wasFullScreen) {
            stage.setFullScreen(true);
        } else if (wasMaximized) {
            stage.setMaximized(true);
        } else if (savedWidth > 0 && savedHeight > 0) {
            stage.setWidth(savedWidth);
            stage.setHeight(savedHeight);
        } else {
            stage.setWidth(defaultWidth);
            stage.setHeight(defaultHeight);
        }
    }

    public static void applyStylesheet(Scene scene) {
        var css = SceneNavigator.class.getResource(STYLESHEET);
        if (css != null && !scene.getStylesheets().contains(css.toExternalForm())) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
