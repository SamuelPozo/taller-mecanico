package com.taller.tallerdesktop.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.tallerdesktop.MainApp;
import com.taller.tallerdesktop.service.AuthService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class PlazasController {

    @FXML private FlowPane plazasContainer;
    @FXML private TableView<Map<String, String>> plazasTable;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colNumero;
    @FXML private TableColumn<Map<String, String>, String> colDescripcion;
    @FXML private TableColumn<Map<String, String>, String> colEstado;
    @FXML private TableColumn<Map<String, String>, String> colAcciones;

    private static final String BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        setupTable();
        loadPlazas();
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("id", "")));
        colNumero.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("numero", "")));
        colDescripcion.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("descripcion", "")));
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("estado", "")));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-background-radius: 20; -fx-padding: 4 10 4 10; " +
                            "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; " +
                            "-fx-background-color: " + getPlazaColor(item) + ";");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox box = new HBox(8, btnEditar, btnEliminar);
            {
                btnEditar.setStyle("-fx-background-color: #0F3460; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnEditar.setOnAction(e -> handleEditarPlaza(getTableRow().getItem()));
                btnEliminar.setOnAction(e -> handleEliminarPlaza(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadPlazas() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/plazas")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode plazas = mapper.readTree(response.body().string());
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();

                        Platform.runLater(() -> plazasContainer.getChildren().clear());

                        for (JsonNode plaza : plazas) {
                            Map<String, String> row = new HashMap<>();
                            row.put("id", plaza.get("id").asText());
                            row.put("numero", plaza.get("numero").asText());
                            row.put("descripcion", plaza.path("descripcion").asText("-"));
                            row.put("estado", plaza.get("estado").asText());
                            data.add(row);

                            String numero = plaza.get("numero").asText();
                            String estado = plaza.get("estado").asText();

                            Platform.runLater(() -> {
                                VBox card = new VBox(6);
                                card.setAlignment(Pos.CENTER);
                                card.setPrefWidth(90);
                                card.setPrefHeight(80);
                                card.setStyle("-fx-background-radius: 10; -fx-padding: 12; " +
                                        "-fx-background-color: " + getPlazaColor(estado) + "; " +
                                        "-fx-cursor: hand;");

                                Label numLabel = new Label(numero);
                                numLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 18px;");

                                Label estadoLabel = new Label(estado);
                                estadoLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.85);");

                                card.getChildren().addAll(numLabel, estadoLabel);
                                plazasContainer.getChildren().add(card);
                            });
                        }

                        Platform.runLater(() -> plazasTable.setItems(data));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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

    @FXML
    private void handleNuevePlaza() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Plaza");
        dialog.setHeaderText("Crear nueva plaza");
        dialog.setContentText("Número de plaza:");
        MainApp.applyDarkDialog(dialog);
        dialog.showAndWait().ifPresent(numero -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(Map.of("numero", numero, "descripcion", ""));
                    RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/plazas")
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .post(body)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(this::loadPlazas);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEditarPlaza(Map<String, String> plaza) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(plaza.get("estado"),
                "LIBRE", "OCUPADA", "RESERVADA", "MANTENIMIENTO");
        dialog.setTitle("Editar Plaza");
        dialog.setHeaderText("Plaza #" + plaza.get("numero"));
        dialog.setContentText("Nuevo estado:");
        MainApp.applyDarkDialog(dialog);
        dialog.showAndWait().ifPresent(nuevoEstado -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(Map.of("estado", nuevoEstado));
                    RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/plazas/" + plaza.get("id"))
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .put(body)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(this::loadPlazas);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEliminarPlaza(Map<String, String> plaza) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar plaza #" + plaza.get("numero") + "?");
        alert.setContentText("Esta acción no se puede deshacer.");
        MainApp.applyDarkDialog(alert);
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Request request = new Request.Builder()
                                .url(BASE_URL + "/admin/plazas/" + plaza.get("id"))
                                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                                .delete()
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()) {
                                Platform.runLater(this::loadPlazas);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }
}