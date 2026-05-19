package com.taller.tallerdesktop.controller;

import com.taller.tallerdesktop.MainApp;
import com.taller.tallerdesktop.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Button btnMostrarPassword;
    @FXML private HBox passwordBox;

    private boolean passwordShown = false;
    private TextField passwordVisible;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        passwordField.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());

        passwordVisible = new TextField();
        passwordVisible.setPromptText("••••••••");
        passwordVisible.getStyleClass().add("login-field");
        HBox.setHgrow(passwordVisible, Priority.ALWAYS);
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordShown ? passwordVisible.getText() : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce el email y la contraseña.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("INICIANDO SESIÓN...");
        errorLabel.setText("");

        new Thread(() -> {
            try {
                AuthService authService = new AuthService();
                String error = authService.login(email, password);

                Platform.runLater(() -> {
                    if (error == null) {
                        try {
                            MainApp.showDashboardView();
                        } catch (Exception ex) {
                            showError("Error al cargar el panel principal.");
                        }
                    } else {
                        showError(error);
                        loginButton.setDisable(false);
                        loginButton.setText("INICIAR SESIÓN");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showError("No se puede conectar con el servidor. Verifica que el backend está en ejecución.");
                    loginButton.setDisable(false);
                    loginButton.setText("INICIAR SESIÓN");
                });
            }
        }).start();
    }

    @FXML
    private void handleTogglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordBox.getChildren().remove(passwordField);
            passwordBox.getChildren().add(0, passwordVisible);
            passwordVisible.requestFocus();
            passwordVisible.positionCaret(passwordVisible.getText().length());
            btnMostrarPassword.setText("🙈");
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordBox.getChildren().remove(passwordVisible);
            passwordBox.getChildren().add(0, passwordField);
            passwordField.requestFocus();
            btnMostrarPassword.setText("👁️");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}