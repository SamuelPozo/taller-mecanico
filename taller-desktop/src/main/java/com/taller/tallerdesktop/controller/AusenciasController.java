package com.taller.tallerdesktop.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.tallerdesktop.service.AuthService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class AusenciasController {

    @FXML private TableView<Map<String, String>> ausenciasTable;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colMecanico;
    @FXML private TableColumn<Map<String, String>, String> colTipo;
    @FXML private TableColumn<Map<String, String>, String> colInicio;
    @FXML private TableColumn<Map<String, String>, String> colFin;
    @FXML private TableColumn<Map<String, String>, String> colMotivo;
    @FXML private TableColumn<Map<String, String>, String> colEstado;
    @FXML private TableColumn<Map<String, String>, String> colAcciones;
    @FXML private ComboBox<String> filtroEstado;

    private static final String BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupFiltro();
        setupTable();
        loadAusencias();
    }

    private void setupFiltro() {
        filtroEstado.setItems(FXCollections.observableArrayList(
                "TODAS", "PENDIENTES", "APROBADAS", "RECHAZADAS"
        ));
        filtroEstado.setValue("TODAS");
        filtroEstado.setOnAction(e -> applyFilter());
    }

    private void applyFilter() {
        String filtro = filtroEstado.getValue();
        if (filtro == null || filtro.equals("TODAS")) {
            ausenciasTable.setItems(allData);
        } else {
            FilteredList<Map<String, String>> filtered = allData.filtered(row -> {
                String estado = row.getOrDefault("estadoRaw", "");
                return switch (filtro) {
                    case "PENDIENTES" -> estado.equals("PENDIENTE");
                    case "APROBADAS" -> estado.equals("APROBADA");
                    case "RECHAZADAS" -> estado.equals("RECHAZADA");
                    default -> true;
                };
            });
            ausenciasTable.setItems(filtered);
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("id", "")));
        colMecanico.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("mecanico", "")));
        colTipo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("tipo", "")));
        colInicio.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("inicio", "")));
        colFin.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("fin", "")));
        colMotivo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("motivo", "")));
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("estado", "")));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                String color = switch (item) {
                    case "PENDIENTE" -> "#F39C12";
                    case "APROBADA" -> "#2ECC71";
                    case "RECHAZADA" -> "#E94560";
                    default -> "#555575";
                };
                badge.setStyle("-fx-background-radius: 20; -fx-padding: 4 10 4 10; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + color + ";");
                setGraphic(badge); setText(null);
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnAprobar = new Button("✅");
            private final Button btnRechazar = new Button("❌");
            private final HBox box = new HBox(8, btnAprobar, btnRechazar);
            {
                btnAprobar.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnRechazar.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnAprobar.setOnAction(e -> handleAprobar(getTableRow().getItem()));
                btnRechazar.setOnAction(e -> handleRechazar(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                String estadoRaw = getTableRow().getItem().getOrDefault("estadoRaw", "");
                setGraphic(estadoRaw.equals("PENDIENTE") ? box : null);
            }
        });
    }

    private void loadAusencias() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/ausencias")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode ausencias = mapper.readTree(response.body().string());
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

                        for (JsonNode a : ausencias) {
                            Map<String, String> row = new HashMap<>();
                            row.put("id", a.get("id").asText());
                            row.put("mecanico", a.path("mecanico").path("usuario")
                                    .path("nombre").asText()
                                    + " " + a.path("mecanico").path("usuario")
                                    .path("apellidos").asText());
                            row.put("tipo", a.get("tipo").asText());
                            row.put("inicio", a.get("fechaInicio").asText());
                            row.put("fin", a.get("fechaFin").asText());
                            row.put("motivo", a.path("motivo").asText("-"));

                            String aprobada = a.path("aprobada").asText("null");
                            String estadoRaw = aprobada.equals("true") ? "APROBADA" :
                                    aprobada.equals("false") ? "RECHAZADA" : "PENDIENTE";
                            row.put("estado", estadoRaw);
                            row.put("estadoRaw", estadoRaw);
                            data.add(row);
                        }

                        Platform.runLater(() -> {
                            allData = data;
                            ausenciasTable.setItems(allData);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleAprobar(Map<String, String> ausencia) {
        actualizarAusencia(ausencia.get("id"), true);
    }

    private void handleRechazar(Map<String, String> ausencia) {
        actualizarAusencia(ausencia.get("id"), false);
    }

    private void actualizarAusencia(String id, boolean aprobada) {
        new Thread(() -> {
            try {
                String json = mapper.writeValueAsString(Map.of("aprobada", aprobada));
                RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/ausencias/" + id)
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .patch(body)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Platform.runLater(this::loadAusencias);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}