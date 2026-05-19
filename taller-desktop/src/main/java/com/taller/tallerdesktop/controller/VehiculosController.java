package com.taller.tallerdesktop.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.tallerdesktop.MainApp;
import com.taller.tallerdesktop.service.AuthService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import okhttp3.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VehiculosController {

    @FXML private TableView<Map<String, String>> vehiculosTable;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colMatricula;
    @FXML private TableColumn<Map<String, String>, String> colMarca;
    @FXML private TableColumn<Map<String, String>, String> colModelo;
    @FXML private TableColumn<Map<String, String>, String> colAnio;
    @FXML private TableColumn<Map<String, String>, String> colColor;
    @FXML private TableColumn<Map<String, String>, String> colCliente;
    @FXML private TableColumn<Map<String, String>, String> colAcciones;
    @FXML private TextField searchField;

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();
    private List<Map<String, String>> clientesCache = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadClientes();
        loadVehiculos();
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("id", "")));
        colMatricula.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("matricula", "")));
        colMarca.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("marca", "")));
        colModelo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("modelo", "")));
        colAnio.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("anio", "")));
        colColor.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("color", "")));
        colCliente.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("cliente", "")));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox box = new HBox(8, btnEditar, btnEliminar);
            {
                btnEditar.setStyle("-fx-background-color: #0F3460; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnEditar.setOnAction(e -> handleEditar(getTableRow().getItem()));
                btnEliminar.setOnAction(e -> handleEliminar(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                vehiculosTable.setItems(allData);
            } else {
                String lower = newVal.toLowerCase();
                FilteredList<Map<String, String>> filtered = allData.filtered(row ->
                        row.getOrDefault("matricula", "").toLowerCase().contains(lower) ||
                                row.getOrDefault("marca", "").toLowerCase().contains(lower) ||
                                row.getOrDefault("modelo", "").toLowerCase().contains(lower) ||
                                row.getOrDefault("cliente", "").toLowerCase().contains(lower)
                );
                vehiculosTable.setItems(filtered);
            }
        });
    }

    private void loadClientes() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/clientes")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode clientes = mapper.readTree(response.body().string());
                        List<Map<String, String>> lista = new java.util.ArrayList<>();
                        for (JsonNode c : clientes) {
                            Map<String, String> m = new HashMap<>();
                            m.put("id", c.get("id").asText());
                            m.put("nombre", c.path("usuario").path("nombre").asText()
                                    + " " + c.path("usuario").path("apellidos").asText());
                            lista.add(m);
                        }
                        clientesCache = lista;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadVehiculos() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/vehiculos")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode vehiculos = mapper.readTree(response.body().string());
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        for (JsonNode v : vehiculos) {
                            Map<String, String> row = new HashMap<>();
                            row.put("id", v.get("id").asText());
                            row.put("matricula", v.get("matricula").asText());
                            row.put("marca", v.get("marca").asText());
                            row.put("modelo", v.get("modelo").asText());
                            row.put("anio", v.get("anio").asText());
                            row.put("color", v.path("color").asText("-"));
                            row.put("clienteId", v.path("cliente").path("id").asText());
                            row.put("cliente", v.path("cliente").path("usuario").path("nombre").asText()
                                    + " " + v.path("cliente").path("usuario").path("apellidos").asText());
                            data.add(row);
                        }
                        Platform.runLater(() -> {
                            allData = data;
                            vehiculosTable.setItems(allData);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleNuevoVehiculo() {
        Dialog<Map<String, String>> dialog = buildFormDialog("Nuevo Vehículo", null);
        MainApp.applyDarkDialog(dialog);
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(data);
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/vehiculos")
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .post(RequestBody.create(json, JSON))
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> {
                                loadVehiculos();
                                showAlert(Alert.AlertType.INFORMATION, "Vehículo creado correctamente.");
                            });
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "";
                            JsonNode errorJson = mapper.readTree(errorBody);
                            String msg = errorJson.path("message").asText("Error al crear el vehículo.");
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, msg));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEditar(Map<String, String> vehiculo) {
        if (vehiculo == null) return;
        Dialog<Map<String, String>> dialog = buildFormDialog("Editar Vehículo", vehiculo);
        MainApp.applyDarkDialog(dialog);
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(data);
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/vehiculos/" + vehiculo.get("id"))
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .put(RequestBody.create(json, JSON))
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> {
                                loadVehiculos();
                                showAlert(Alert.AlertType.INFORMATION, "Vehículo actualizado correctamente.");
                            });
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al actualizar el vehículo."));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEliminar(Map<String, String> vehiculo) {
        if (vehiculo == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar vehículo " + vehiculo.get("matricula") + "?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        MainApp.applyDarkDialog(confirm);
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Request request = new Request.Builder()
                                .url(BASE_URL + "/admin/vehiculos/" + vehiculo.get("id"))
                                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                                .delete()
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()) {
                                Platform.runLater(() -> {
                                    loadVehiculos();
                                    showAlert(Alert.AlertType.INFORMATION, "Vehículo eliminado correctamente.");
                                });
                            } else {
                                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al eliminar el vehículo."));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private Dialog<Map<String, String>> buildFormDialog(String titulo, Map<String, String> vehiculo) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E1E3A;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        String labelStyle = "-fx-text-fill: #A0A0B0; -fx-font-size: 12px;";
        String fieldStyle = "-fx-background-color: #252545; -fx-text-fill: white; -fx-border-color: #2A2A4A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;";

        boolean editando = vehiculo != null;

        TextField tfMatricula = new TextField(editando ? vehiculo.get("matricula") : "");
        TextField tfMarca = new TextField(editando ? vehiculo.get("marca") : "");
        TextField tfModelo = new TextField(editando ? vehiculo.get("modelo") : "");
        TextField tfAnio = new TextField(editando ? vehiculo.get("anio") : "");
        TextField tfColor = new TextField(editando ? vehiculo.getOrDefault("color", "") : "");

        ObservableList<String> clienteNombres = FXCollections.observableArrayList(
                clientesCache.stream().map(c -> c.get("nombre")).toList()
        );
        ComboBox<String> cbCliente = new ComboBox<>(clienteNombres);
        if (editando) cbCliente.setValue(vehiculo.get("cliente"));
        cbCliente.setStyle(fieldStyle);
        cbCliente.setPrefWidth(200);
        cbCliente.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
        });

        for (TextField tf : new TextField[]{tfMatricula, tfMarca, tfModelo, tfAnio, tfColor}) {
            tf.setStyle(fieldStyle);
        }

        int row = 0;
        grid.add(new Label("Matrícula *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfMatricula, 1, row++);
        grid.add(new Label("Marca *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfMarca, 1, row++);
        grid.add(new Label("Modelo *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfModelo, 1, row++);
        grid.add(new Label("Año") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfAnio, 1, row++);
        grid.add(new Label("Color") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfColor, 1, row++);
        grid.add(new Label("Cliente *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(cbCliente, 1, row++);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String matricula = tfMatricula.getText().trim();
            String marca = tfMarca.getText().trim();
            String modelo = tfModelo.getText().trim();
            String anio = tfAnio.getText().trim();

            if (matricula.isBlank() || marca.isBlank() || modelo.isBlank() || cbCliente.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Los campos marcados con * son obligatorios.");
                event.consume();
                return;
            }
            if (!matricula.matches("^[0-9]{4}[A-Za-z]{3}$")) {
                showAlert(Alert.AlertType.WARNING, "La matrícula no es válida. Formato esperado: 1234ABC.");
                event.consume();
                return;
            }
            if (!anio.isBlank()) {
                try {
                    int anioNum = Integer.parseInt(anio);
                    if (anioNum < 1900 || anioNum > 2026) {
                        showAlert(Alert.AlertType.WARNING, "El año debe estar entre 1900 y 2026.");
                        event.consume();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "El año debe ser un número válido.");
                    event.consume();
                    return;
                }
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String clienteId = clientesCache.stream()
                        .filter(c -> c.get("nombre").equals(cbCliente.getValue()))
                        .map(c -> c.get("id"))
                        .findFirst().orElse(null);
                Map<String, String> data = new HashMap<>();
                data.put("matricula", tfMatricula.getText().trim().toUpperCase());
                data.put("marca", tfMarca.getText().trim());
                data.put("modelo", tfModelo.getText().trim());
                data.put("anio", tfAnio.getText().trim());
                data.put("color", tfColor.getText().trim());
                data.put("clienteId", clienteId);
                return data;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        MainApp.applyDarkDialog(alert);
        alert.showAndWait();
    }
}