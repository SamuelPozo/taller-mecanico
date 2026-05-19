package com.taller.tallerdesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.Dialog;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static double xOffset = 0;
    private static double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);
        showLoginView();
    }

    public static void showLoginView() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/taller/tallerdesktop/view/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(MainApp.class.getResource("/com/taller/tallerdesktop/css/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showDashboardView() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/taller/tallerdesktop/view/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(MainApp.class.getResource("/com/taller/tallerdesktop/css/styles.css").toExternalForm());
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void enableDrag(javafx.scene.Node node) {
        node.setOnMousePressed((MouseEvent e) -> {
            if (!primaryStage.isMaximized()) {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            }
        });
        node.setOnMouseDragged((MouseEvent e) -> {
            if (!primaryStage.isMaximized()) {
                primaryStage.setX(e.getScreenX() - xOffset);
                primaryStage.setY(e.getScreenY() - yOffset);
            }
        });
    }

    public static void applyDarkDialog(Dialog<?> dialog) {
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E1E3A; -fx-border-color: #2A2A4A; -fx-border-width: 1;");

        javafx.scene.layout.HBox titleBar = new javafx.scene.layout.HBox();
        titleBar.setStyle("-fx-background-color: #0F3460; -fx-padding: 0 12 0 16; -fx-min-height: 36; -fx-max-height: 36;");
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(dialog.getTitle());
        titleLabel.setStyle("-fx-text-fill: #A0A0B0; -fx-font-size: 12px; -fx-font-family: 'Segoe UI';");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button btnCerrar = new javafx.scene.control.Button("✕");
        String btnStyle = "-fx-background-color: transparent; -fx-text-fill: #A0A0B0; -fx-font-size: 13px; -fx-cursor: hand; -fx-pref-width: 40; -fx-pref-height: 36; -fx-background-radius: 0; -fx-border-width: 0; -fx-padding: 0;";
        String btnHoverStyle = "-fx-background-color: #E94560; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-pref-width: 40; -fx-pref-height: 36; -fx-background-radius: 0; -fx-border-width: 0; -fx-padding: 0;";
        btnCerrar.setStyle(btnStyle);
        btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle(btnHoverStyle));
        btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle(btnStyle));
        btnCerrar.setOnAction(e -> dialog.close());

        titleBar.getChildren().addAll(titleLabel, spacer, btnCerrar);

        final double[] offset = {0, 0};
        titleBar.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });
        titleBar.setOnMouseDragged(e -> {
            dialog.setX(e.getScreenX() - offset[0]);
            dialog.setY(e.getScreenY() - offset[1]);
        });

        dialog.getDialogPane().setHeader(titleBar);
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Node content = dialog.getDialogPane().lookup(".content.label");
            if (content instanceof javafx.scene.control.Label lbl) {
                lbl.setStyle("-fx-text-fill: #EAEAEA; -fx-font-size: 13px;");
            }
            javafx.scene.Node header = dialog.getDialogPane().lookup(".header-panel .label");
            if (header instanceof javafx.scene.control.Label lbl) {
                lbl.setStyle("-fx-text-fill: #EAEAEA; -fx-font-size: 14px; -fx-font-weight: bold;");
            }
        });
    }


    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}