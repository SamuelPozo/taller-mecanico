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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrdenesController {

    @FXML private TableView<Map<String, String>> ordenesTable;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colVehiculo;
    @FXML private TableColumn<Map<String, String>, String> colCliente;
    @FXML private TableColumn<Map<String, String>, String> colMecanico;
    @FXML private TableColumn<Map<String, String>, String> colEstado;
    @FXML private TableColumn<Map<String, String>, String> colPrecio;
    @FXML private TableColumn<Map<String, String>, String> colFecha;
    @FXML private TableColumn<Map<String, String>, String> colAcciones;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtroEstado;

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();
    private List<Map<String, String>> vehiculosCache = new ArrayList<>();
    private List<Map<String, String>> mecanicosCache = new ArrayList<>();

    @FXML
    public void initialize() {
        setupFiltro();
        setupTable();
        setupSearch();
        loadCaches();
        loadOrdenes();
    }

    private void setupFiltro() {
        filtroEstado.setItems(FXCollections.observableArrayList(
                "TODOS", "PENDIENTE", "EN_PROCESO", "DIAGNOSTICADO",
                "REPARADO", "LISTO", "ENTREGADO"
        ));
        filtroEstado.setValue("TODOS");
        filtroEstado.setOnAction(e -> applyFilters());
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("id", "")));
        colVehiculo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("vehiculo", "")));
        colCliente.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("cliente", "")));
        colMecanico.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("mecanico", "")));
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("estado", "")));
        colPrecio.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("precio", "")));
        colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("fecha", "")));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setStyle("-fx-background-radius: 20; -fx-padding: 4 10 4 10; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + getEstadoColor(item) + ";");
                setGraphic(badge); setText(null);
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnEstado = new Button("🔄");
            private final Button btnEliminar = new Button("🗑️");
            private final HBox box = new HBox(6, btnVer, btnEstado, btnEliminar);
            {
                btnVer.setStyle("-fx-background-color: #0F3460; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnEstado.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnVer.setOnAction(e -> handleVer(getTableRow().getItem()));
                btnEstado.setOnAction(e -> handleCambiarEstado(getTableRow().getItem()));
                btnEliminar.setOnAction(e -> handleEliminar(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private String getEstadoColor(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "#F39C12";
            case "EN_PROCESO" -> "#3498DB";
            case "DIAGNOSTICADO" -> "#9B59B6";
            case "REPARADO" -> "#1ABC9C";
            case "LISTO" -> "#2ECC71";
            case "ENTREGADO" -> "#555575";
            default -> "#555575";
        };
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String estado = filtroEstado.getValue();
        FilteredList<Map<String, String>> filtered = allData.filtered(row -> {
            boolean matchSearch = search.isEmpty() ||
                    row.getOrDefault("vehiculo", "").toLowerCase().contains(search) ||
                    row.getOrDefault("cliente", "").toLowerCase().contains(search) ||
                    row.getOrDefault("mecanico", "").toLowerCase().contains(search);
            boolean matchEstado = estado == null || estado.equals("TODOS") ||
                    row.getOrDefault("estado", "").equals(estado);
            return matchSearch && matchEstado;
        });
        ordenesTable.setItems(filtered);
    }

    private void loadCaches() {
        new Thread(() -> {
            try {
                Request reqV = new Request.Builder()
                        .url(BASE_URL + "/admin/vehiculos")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response r = client.newCall(reqV).execute()) {
                    if (r.isSuccessful() && r.body() != null) {
                        JsonNode vehiculos = mapper.readTree(r.body().string());
                        List<Map<String, String>> lista = new ArrayList<>();
                        for (JsonNode v : vehiculos) {
                            Map<String, String> m = new HashMap<>();
                            m.put("id", v.get("id").asText());
                            m.put("label", v.get("marca").asText() + " " + v.get("modelo").asText()
                                    + " (" + v.get("matricula").asText() + ")");
                            lista.add(m);
                        }
                        vehiculosCache = lista;
                    }
                }
                Request reqM = new Request.Builder()
                        .url(BASE_URL + "/admin/mecanicos")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response r = client.newCall(reqM).execute()) {
                    if (r.isSuccessful() && r.body() != null) {
                        JsonNode mecanicos = mapper.readTree(r.body().string());
                        List<Map<String, String>> lista = new ArrayList<>();
                        for (JsonNode m : mecanicos) {
                            Map<String, String> map = new HashMap<>();
                            map.put("id", m.get("id").asText());
                            map.put("nombre", m.path("usuario").path("nombre").asText()
                                    + " " + m.path("usuario").path("apellidos").asText());
                            lista.add(map);
                        }
                        mecanicosCache = lista;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadOrdenes() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/ordenes")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode ordenes = mapper.readTree(response.body().string());
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        for (JsonNode o : ordenes) {
                            Map<String, String> row = new HashMap<>();
                            row.put("id", o.get("id").asText());
                            row.put("vehiculoId", o.path("vehiculo").path("id").asText());
                            row.put("mecanicoId", o.path("mecanico").path("id").asText());
                            row.put("vehiculo", o.path("vehiculo").path("marca").asText()
                                    + " " + o.path("vehiculo").path("modelo").asText()
                                    + " (" + o.path("vehiculo").path("matricula").asText() + ")");
                            row.put("cliente", o.path("vehiculo").path("cliente").path("usuario").path("nombre").asText()
                                    + " " + o.path("vehiculo").path("cliente").path("usuario").path("apellidos").asText());
                            row.put("mecanico", o.path("mecanico").path("usuario").path("nombre").asText()
                                    + " " + o.path("mecanico").path("usuario").path("apellidos").asText());
                            row.put("estado", o.get("estado").asText());
                            row.put("precio", o.path("precioEstimado").asText("-") + " €");
                            row.put("precioFinal", o.path("precioFinal").asText(""));
                            row.put("diagnostico", o.path("diagnostico").asText(""));
                            row.put("descripcion", o.path("descripcionProblema").asText(""));
                            row.put("notas", o.path("notasInternas").asText(""));
                            row.put("fecha", o.get("fechaEntrada").asText().substring(0, 10));
                            data.add(row);
                        }
                        Platform.runLater(() -> {
                            allData = data;
                            applyFilters();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleNuevaOrden() {
        Dialog<Map<String, String>> dialog = buildFormDialog();
        MainApp.applyDarkDialog(dialog);
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(data);
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/ordenes")
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .post(RequestBody.create(json, JSON))
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> {
                                loadOrdenes();
                                showAlert(Alert.AlertType.INFORMATION, "Orden creada correctamente.");
                            });
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "";
                            JsonNode errorJson = mapper.readTree(errorBody);
                            String msg = errorJson.path("message").asText("Error al crear la orden.");
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, msg));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleVer(Map<String, String> orden) {
        if (orden == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de Orden #" + orden.get("id"));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E1E3A;");

        String labelStyle = "-fx-text-fill: #A0A0B0; -fx-font-size: 12px;";
        String valueStyle = "-fx-text-fill: #EAEAEA; -fx-font-size: 13px; -fx-font-weight: bold;";

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        int row = 0;
        String[][] campos = {
                {"Vehículo", orden.get("vehiculo")},
                {"Cliente", orden.get("cliente")},
                {"Mecánico", orden.get("mecanico")},
                {"Estado", orden.get("estado")},
                {"Descripción", orden.get("descripcion")},
                {"Diagnóstico", orden.get("diagnostico").isEmpty() ? "—" : orden.get("diagnostico")},
                {"Precio estimado", orden.get("precio")},
                {"Precio final", orden.get("precioFinal").isEmpty() ? "—" : orden.get("precioFinal") + " €"},
                {"Fecha entrada", orden.get("fecha")},
                {"Notas internas", orden.get("notas").isEmpty() ? "—" : orden.get("notas")}
        };

        for (String[] campo : campos) {
            Label label = new Label(campo[0]);
            label.setStyle(labelStyle);
            Label value = new Label(campo[1]);
            value.setStyle(valueStyle);
            value.setWrapText(true);
            value.setMaxWidth(300);
            grid.add(label, 0, row);
            grid.add(value, 1, row++);
        }

        dialog.getDialogPane().setContent(grid);
        MainApp.applyDarkDialog(dialog);
        dialog.showAndWait();
    }

    private void handleCambiarEstado(Map<String, String> orden) {
        if (orden == null) return;

        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Estado — Orden #" + orden.get("id"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E1E3A;");

        String labelStyle = "-fx-text-fill: #A0A0B0; -fx-font-size: 12px;";
        String fieldStyle = "-fx-background-color: #252545; -fx-text-fill: white; -fx-border-color: #2A2A4A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;";

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> cbEstado = new ComboBox<>(FXCollections.observableArrayList(
                "PENDIENTE", "EN_PROCESO", "DIAGNOSTICADO", "REPARADO", "LISTO", "ENTREGADO"
        ));
        cbEstado.setValue(orden.get("estado"));
        cbEstado.setStyle(fieldStyle);
        cbEstado.setPrefWidth(200);
        cbEstado.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
        });

        TextField tfObservacion = new TextField();
        tfObservacion.setStyle(fieldStyle);
        tfObservacion.setPromptText("Observación (opcional)");

        grid.add(new Label("Nuevo estado *") {{ setStyle(labelStyle); }}, 0, 0);
        grid.add(cbEstado, 1, 0);
        grid.add(new Label("Observación") {{ setStyle(labelStyle); }}, 0, 1);
        grid.add(tfObservacion, 1, 1);

        dialog.getDialogPane().setContent(grid);
        MainApp.applyDarkDialog(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (cbEstado.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Debes seleccionar un estado.");
                event.consume();
                return;
            }
            if (cbEstado.getValue().equals(orden.get("estado"))) {
                showAlert(Alert.AlertType.WARNING, "El estado seleccionado es el mismo que el actual.");
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Map<String, String> data = new HashMap<>();
                data.put("estado", cbEstado.getValue());
                data.put("observacion", tfObservacion.getText().trim());
                return data;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(data);
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/ordenes/" + orden.get("id") + "/estado")
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .patch(RequestBody.create(json, JSON))
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> {
                                loadOrdenes();
                                showAlert(Alert.AlertType.INFORMATION, "Estado actualizado correctamente.");
                            });
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al cambiar el estado."));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEliminar(Map<String, String> orden) {
        if (orden == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar orden #" + orden.get("id") + "?");
        confirm.setContentText("Vehículo: " + orden.get("vehiculo") + "\nEsta acción no se puede deshacer.");
        MainApp.applyDarkDialog(confirm);
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Request request = new Request.Builder()
                                .url(BASE_URL + "/admin/ordenes/" + orden.get("id"))
                                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                                .delete()
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()) {
                                Platform.runLater(() -> {
                                    loadOrdenes();
                                    showAlert(Alert.AlertType.INFORMATION, "Orden eliminada correctamente.");
                                });
                            } else {
                                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al eliminar la orden."));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private Dialog<Map<String, String>> buildFormDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Nueva Orden de Trabajo");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E1E3A;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        String labelStyle = "-fx-text-fill: #A0A0B0; -fx-font-size: 12px;";
        String fieldStyle = "-fx-background-color: #252545; -fx-text-fill: white; -fx-border-color: #2A2A4A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;";

        ObservableList<String> vehiculoLabels = FXCollections.observableArrayList(
                vehiculosCache.stream().map(v -> v.get("label")).toList()
        );
        ComboBox<String> cbVehiculo = new ComboBox<>(vehiculoLabels);
        cbVehiculo.setStyle(fieldStyle);
        cbVehiculo.setPrefWidth(250);
        cbVehiculo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
        });

        ObservableList<String> mecanicoNombres = FXCollections.observableArrayList(
                mecanicosCache.stream().map(m -> m.get("nombre")).toList()
        );
        ComboBox<String> cbMecanico = new ComboBox<>(mecanicoNombres);
        cbMecanico.setStyle(fieldStyle);
        cbMecanico.setPrefWidth(250);
        cbMecanico.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
        });

        TextArea taDescripcion = new TextArea();
        taDescripcion.setStyle(fieldStyle + " -fx-control-inner-background: #252545; -fx-prompt-text-fill: #555575;");
        taDescripcion.setPrefRowCount(3);
        taDescripcion.setPromptText("Descripción del problema...");

        TextArea taNotas = new TextArea();
        taNotas.setStyle(fieldStyle + " -fx-control-inner-background: #252545; -fx-prompt-text-fill: #555575;");
        taNotas.setPrefRowCount(2);
        taNotas.setPromptText("Notas internas (opcional)...");

        int row = 0;
        grid.add(new Label("Vehículo *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(cbVehiculo, 1, row++);
        grid.add(new Label("Mecánico *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(cbMecanico, 1, row++);
        grid.add(new Label("Descripción *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(taDescripcion, 1, row++);
        grid.add(new Label("Notas internas") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(taNotas, 1, row++);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (cbVehiculo.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Debes seleccionar un vehículo.");
                event.consume();
                return;
            }
            if (cbMecanico.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Debes seleccionar un mecánico.");
                event.consume();
                return;
            }
            if (taDescripcion.getText().isBlank()) {
                showAlert(Alert.AlertType.WARNING, "La descripción del problema es obligatoria.");
                event.consume();
                return;
            }
            if (taDescripcion.getText().trim().length() < 10) {
                showAlert(Alert.AlertType.WARNING, "La descripción debe tener al menos 10 caracteres.");
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String vehiculoId = vehiculosCache.stream()
                        .filter(v -> v.get("label").equals(cbVehiculo.getValue()))
                        .map(v -> v.get("id")).findFirst().orElse(null);
                String mecanicoId = mecanicosCache.stream()
                        .filter(m -> m.get("nombre").equals(cbMecanico.getValue()))
                        .map(m -> m.get("id")).findFirst().orElse(null);
                Map<String, String> data = new HashMap<>();
                data.put("vehiculoId", vehiculoId);
                data.put("mecanicoId", mecanicoId);
                data.put("descripcionProblema", taDescripcion.getText().trim());
                data.put("notasInternas", taNotas.getText().trim());
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