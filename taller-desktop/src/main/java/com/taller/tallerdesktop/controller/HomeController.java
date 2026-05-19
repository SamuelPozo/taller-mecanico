package com.taller.tallerdesktop.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.tallerdesktop.service.AuthService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.HashMap;
import java.util.Map;

public class HomeController {

    @FXML private Label totalVehiculos;
    @FXML private Label ordenesPendientes;
    @FXML private Label ordenesEnProceso;
    @FXML private Label ordenesListas;
    @FXML private TableView<Map<String, String>> ordenesTable;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colVehiculo;
    @FXML private TableColumn<Map<String, String>, String> colCliente;
    @FXML private TableColumn<Map<String, String>, String> colMecanico;
    @FXML private TableColumn<Map<String, String>, String> colEstado;
    @FXML private TableColumn<Map<String, String>, String> colFecha;
    @FXML private FlowPane plazasContainer;
    @FXML private VBox notificacionesContainer;

    private static final String BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("id", "")));
        colVehiculo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("vehiculo", "")));
        colCliente.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("cliente", "")));
        colMecanico.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("mecanico", "")));
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("estado", "")));
        colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("fecha", "")));
    }

    private void loadData() {
        new Thread(() -> {
            try {
                cargarOrdenes();
                cargarPlazas();
                cargarNotificaciones();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cargarOrdenes() throws Exception {
        Request request = new Request.Builder()
                .url(BASE_URL + "/admin/ordenes")
                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonNode ordenes = mapper.readTree(response.body().string());
                ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                int pendientes = 0, enProceso = 0, listas = 0;

                for (JsonNode orden : ordenes) {
                    Map<String, String> row = new HashMap<>();
                    row.put("id", orden.get("id").asText());
                    row.put("vehiculo", orden.path("vehiculo").path("marca").asText()
                            + " " + orden.path("vehiculo").path("modelo").asText()
                            + " (" + orden.path("vehiculo").path("matricula").asText() + ")");
                    row.put("cliente", orden.path("vehiculo").path("cliente")
                            .path("usuario").path("nombre").asText()
                            + " " + orden.path("vehiculo").path("cliente")
                            .path("usuario").path("apellidos").asText());
                    row.put("mecanico", orden.path("mecanico").path("usuario")
                            .path("nombre").asText()
                            + " " + orden.path("mecanico").path("usuario")
                            .path("apellidos").asText());
                    row.put("estado", orden.get("estado").asText());
                    row.put("fecha", orden.get("fechaEntrada").asText().substring(0, 10));
                    data.add(row);

                    String estado = orden.get("estado").asText();
                    if (estado.equals("PENDIENTE")) pendientes++;
                    else if (estado.equals("EN_PROCESO") || estado.equals("DIAGNOSTICADO")) enProceso++;
                    else if (estado.equals("LISTO") || estado.equals("REPARADO")) listas++;
                }

                final int p = pendientes, ep = enProceso, l = listas;
                final int total = data.size();

                Platform.runLater(() -> {
                    ordenesTable.setItems(data);
                    totalVehiculos.setText(String.valueOf(total));
                    ordenesPendientes.setText(String.valueOf(p));
                    ordenesEnProceso.setText(String.valueOf(ep));
                    ordenesListas.setText(String.valueOf(l));
                });
            }
        }
    }

    private void cargarPlazas() throws Exception {
        Request request = new Request.Builder()
                .url(BASE_URL + "/admin/plazas")
                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonNode plazas = mapper.readTree(response.body().string());

                Platform.runLater(() -> {
                    plazasContainer.getChildren().clear();
                    for (JsonNode plaza : plazas) {
                        String numero = plaza.get("numero").asText();
                        String estado = plaza.get("estado").asText();

                        VBox plazaCard = new VBox(4);
                        plazaCard.setAlignment(javafx.geometry.Pos.CENTER);
                        plazaCard.setPrefWidth(80);
                        plazaCard.setPrefHeight(70);
                        plazaCard.setStyle(
                                "-fx-background-radius: 8; -fx-padding: 10; " +
                                        "-fx-background-color: " + getPlazaColor(estado) + ";"
                        );

                        Label numLabel = new Label(numero);
                        numLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;");

                        Label estadoLabel = new Label(estado);
                        estadoLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.8);");

                        plazaCard.getChildren().addAll(numLabel, estadoLabel);
                        plazasContainer.getChildren().add(plazaCard);
                    }
                });
            }
        }
    }

    private String getPlazaColor(String estado) {
        return switch (estado) {
            case "LIBRE" -> "#2ECC71";
            case "OCUPADA" -> "#E94560";
            case "RESERVADA" -> "#F39C12";
            case "MANTENIMIENTO" -> "#95A5A6";
            default -> "#555575";
        };
    }

    private void cargarNotificaciones() throws Exception {
        Request request = new Request.Builder()
                .url(BASE_URL + "/notificaciones")
                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonNode notificaciones = mapper.readTree(response.body().string());

                Platform.runLater(() -> {
                    notificacionesContainer.getChildren().clear();

                    if (!notificaciones.isArray() || notificaciones.size() == 0) {
                        Label vacio = new Label("No hay notificaciones recientes.");
                        vacio.setStyle("-fx-text-fill: #A0A0B0; -fx-font-size: 12px;");
                        notificacionesContainer.getChildren().add(vacio);
                        return;
                    }

                    int count = 0;
                    for (JsonNode n : notificaciones) {
                        if (count >= 10) break;

                        boolean leida = n.path("leida").asBoolean(false);
                        String titulo = n.path("titulo").asText("");
                        String mensaje = n.path("mensaje").asText("");
                        String fecha = n.path("fecha").asText("").replace("T", " ");
                        if (fecha.length() > 16) fecha = fecha.substring(0, 16);

                        VBox card = new VBox(4);
                        card.setPadding(new javafx.geometry.Insets(10, 14, 10, 14));
                        card.setStyle(
                                "-fx-background-color: " + (leida ? "#1A1A2E" : "#1E1E3A") + ";" +
                                        "-fx-background-radius: 8;" +
                                        "-fx-border-color: " + (leida ? "#2A2A4A" : "#E94560") + ";" +
                                        "-fx-border-width: 0 0 0 3;" +
                                        "-fx-border-radius: 0 8 8 0;"
                        );

                        Label lblTitulo = new Label((leida ? "" : "🔴 ") + titulo);
                        lblTitulo.setStyle("-fx-text-fill: " + (leida ? "#A0A0B0" : "#EAEAEA") + "; -fx-font-weight: bold; -fx-font-size: 12px;");

                        Label lblMensaje = new Label(mensaje);
                        lblMensaje.setStyle("-fx-text-fill: #A0A0B0; -fx-font-size: 11px;");
                        lblMensaje.setWrapText(true);

                        Label lblFecha = new Label(fecha);
                        lblFecha.setStyle("-fx-text-fill: #555575; -fx-font-size: 10px;");

                        card.getChildren().addAll(lblTitulo, lblMensaje, lblFecha);
                        notificacionesContainer.getChildren().add(card);
                        count++;
                    }
                });
            }
        }
    }
}