package com.taller.tallerdesktop.controller;

import com.taller.tallerdesktop.MainApp;
import com.taller.tallerdesktop.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML private javafx.scene.layout.HBox titleBar;
    @FXML private Button btnMinimizar;
    @FXML private Button btnMaximizar;
    @FXML private Button btnCerrar;
    @FXML private StackPane contentArea;
    @FXML private Label topbarTitle;
    @FXML private Label topbarUser;
    @FXML private Button btnDashboard;
    @FXML private Button btnVehiculos;
    @FXML private Button btnOrdenes;
    @FXML private Button btnPlazas;
    @FXML private Button btnMecanicos;
    @FXML private Button btnClientes;
    @FXML private Button btnHorarios;
    @FXML private Button btnAusencias;

    private List<Button> sidebarButtons;

    @FXML
    public void initialize() {
        topbarUser.setText(AuthService.getUserName());
        sidebarButtons = List.of(btnDashboard, btnVehiculos, btnOrdenes,
                btnPlazas, btnMecanicos, btnClientes, btnHorarios, btnAusencias);
        showDashboard();
        MainApp.enableDrag(titleBar);

        // Hover effects en botones de ventana
        btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle(btnCerrar.getStyle().replace("transparent", "#E94560")));
        btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle(btnCerrar.getStyle().replace("#E94560", "transparent")));
        btnMinimizar.setOnMouseEntered(e -> btnMinimizar.setStyle(btnMinimizar.getStyle().replace("transparent", "#252545")));
        btnMinimizar.setOnMouseExited(e -> btnMinimizar.setStyle(btnMinimizar.getStyle().replace("#252545", "transparent")));
        btnMaximizar.setOnMouseEntered(e -> btnMaximizar.setStyle(btnMaximizar.getStyle().replace("transparent", "#252545")));
        btnMaximizar.setOnMouseExited(e -> btnMaximizar.setStyle(btnMaximizar.getStyle().replace("#252545", "transparent")));
    }

    private void setActiveButton(Button active) {
        for (Button btn : sidebarButtons) {
            btn.getStyleClass().removeAll("sidebar-btn-active");
            if (!btn.getStyleClass().contains("sidebar-btn")) {
                btn.getStyleClass().add("sidebar-btn");
            }
        }
        active.getStyleClass().add("sidebar-btn-active");
    }

    private void loadView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/com/taller/tallerdesktop/view/" + fxmlFile));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            topbarTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
            topbarTitle.setText("Error al cargar: " + fxmlFile);
        }
    }

    @FXML
    private void handleMinimizar() {
        MainApp.getPrimaryStage().setIconified(true);
    }

    @FXML
    private void handleMaximizar() {
        Stage stage = MainApp.getPrimaryStage();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void handleCerrarVentana() {
        MainApp.getPrimaryStage().close();
    }

    @FXML public void showDashboard() {
        setActiveButton(btnDashboard);
        loadView("home.fxml", "Dashboard");
    }

    @FXML public void showVehiculos() {
        setActiveButton(btnVehiculos);
        loadView("vehiculos.fxml", "Vehículos");
    }

    @FXML public void showOrdenes() {
        setActiveButton(btnOrdenes);
        loadView("ordenes.fxml", "Órdenes de Trabajo");
    }

    @FXML public void showPlazas() {
        setActiveButton(btnPlazas);
        loadView("plazas.fxml", "Plazas del Taller");
    }

    @FXML public void showMecanicos() {
        setActiveButton(btnMecanicos);
        loadView("mecanicos.fxml", "Mecánicos");
    }

    @FXML public void showClientes() {
        setActiveButton(btnClientes);
        loadView("clientes.fxml", "Clientes");
    }

    @FXML public void showHorarios() {
        setActiveButton(btnHorarios);
        loadView("horarios.fxml", "Horarios");
    }

    @FXML public void showAusencias() {
        setActiveButton(btnAusencias);
        loadView("ausencias.fxml", "Ausencias");
    }

    @FXML public void handleLogout() {
        AuthService.logout();
        try {
            MainApp.showLoginView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}