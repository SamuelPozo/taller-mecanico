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

public class HorariosController {

    @FXML private TableView<Map<String, String>> horariosTable;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colMecanico;
    @FXML private TableColumn<Map<String, String>, String> colDia;
    @FXML private TableColumn<Map<String, String>, String> colEntrada;
    @FXML private TableColumn<Map<String, String>, String> colSalida;
    @FXML private TableColumn<Map<String, String>, String> colAcciones;
    @FXML private ComboBox<String> filtroMecanico;

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();
    private List<Map<String, String>> mecanicosCache = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTable();
        loadMecanicos();
        loadHorarios();
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("id", "")));
        colMecanico.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("mecanico", "")));
        colDia.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("dia", "")));
        colEntrada.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("entrada", "")));
        colSalida.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("salida", "")));

        colDia.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                String color = switch (item) {
                    case "SABADO", "DOMINGO" -> "#E94560";
                    default -> "#0F3460";
                };
                badge.setStyle("-fx-background-radius: 20; -fx-padding: 4 10 4 10; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + color + ";");
                setGraphic(badge); setText(null);
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("🗑️");
            private final HBox box = new HBox(8, btnEliminar);
            {
                btnEliminar.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnEliminar.setOnAction(e -> handleEliminar(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadMecanicos() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/mecanicos")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode mecanicos = mapper.readTree(response.body().string());
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

    private void loadHorarios() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "/admin/horarios")
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode horarios = mapper.readTree(response.body().string());
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        ObservableList<String> mecanicosFilter = FXCollections.observableArrayList("TODOS");

                        for (JsonNode h : horarios) {
                            Map<String, String> row = new HashMap<>();
                            row.put("id", h.get("id").asText());
                            row.put("mecanicoId", h.path("mecanico").path("id").asText());
                            String nombreMecanico = h.path("mecanico").path("usuario").path("nombre").asText()
                                    + " " + h.path("mecanico").path("usuario").path("apellidos").asText();
                            row.put("mecanico", nombreMecanico);
                            row.put("dia", h.get("diaSemana").asText());
                            row.put("entrada", h.get("horaEntrada").asText());
                            row.put("salida", h.get("horaSalida").asText());
                            data.add(row);
                            if (!mecanicosFilter.contains(nombreMecanico))
                                mecanicosFilter.add(nombreMecanico);
                        }

                        Platform.runLater(() -> {
                            allData = data;
                            horariosTable.setItems(allData);
                            filtroMecanico.setItems(mecanicosFilter);
                            filtroMecanico.setValue("TODOS");
                            filtroMecanico.setOnAction(e -> {
                                String selected = filtroMecanico.getValue();
                                if (selected == null || selected.equals("TODOS")) {
                                    horariosTable.setItems(allData);
                                } else {
                                    horariosTable.setItems(allData.filtered(
                                            row -> row.getOrDefault("mecanico", "").equals(selected)));
                                }
                            });
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleNuevoHorario() {
        Dialog<Map<String, String>> dialog = buildFormDialog();
        MainApp.applyDarkDialog(dialog);
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(data);
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/horarios")
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .post(RequestBody.create(json, JSON))
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> {
                                loadHorarios();
                                showAlert(Alert.AlertType.INFORMATION, "Horario creado correctamente.");
                            });
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "";
                            JsonNode errorJson = mapper.readTree(errorBody);
                            String msg = errorJson.path("message").asText("Error al crear el horario.");
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, msg));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEliminar(Map<String, String> horario) {
        if (horario == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar horario?");
        confirm.setContentText(horario.get("mecanico") + " — " + horario.get("dia")
                + " (" + horario.get("entrada") + " - " + horario.get("salida") + ")");
        MainApp.applyDarkDialog(confirm);
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Request request = new Request.Builder()
                                .url(BASE_URL + "/admin/horarios/" + horario.get("id"))
                                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                                .delete()
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()) {
                                Platform.runLater(() -> {
                                    loadHorarios();
                                    showAlert(Alert.AlertType.INFORMATION, "Horario eliminado correctamente.");
                                });
                            } else {
                                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al eliminar el horario."));
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
        dialog.setTitle("Nuevo Horario");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1E1E3A;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        String labelStyle = "-fx-text-fill: #A0A0B0; -fx-font-size: 12px;";
        String fieldStyle = "-fx-background-color: #252545; -fx-text-fill: white; -fx-border-color: #2A2A4A; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;";

        ObservableList<String> nombresMecanicos = FXCollections.observableArrayList(
                mecanicosCache.stream().map(m -> m.get("nombre")).toList()
        );
        ComboBox<String> cbMecanico = new ComboBox<>(nombresMecanicos);
        cbMecanico.setStyle(fieldStyle);
        cbMecanico.setPrefWidth(200);
        cbMecanico.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
        });

        ComboBox<String> cbDia = new ComboBox<>(FXCollections.observableArrayList(
                "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
        ));
        cbDia.setValue("LUNES");
        cbDia.setStyle(fieldStyle);
        cbDia.setPrefWidth(200);
        cbDia.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            }
        });

        TextField tfEntrada = new TextField("08:00");
        TextField tfSalida = new TextField("16:00");
        tfEntrada.setStyle(fieldStyle);
        tfSalida.setStyle(fieldStyle);

        int row = 0;
        grid.add(new Label("Mecánico *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(cbMecanico, 1, row++);
        grid.add(new Label("Día *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(cbDia, 1, row++);
        grid.add(new Label("Hora entrada *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfEntrada, 1, row++);
        grid.add(new Label("Hora salida *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfSalida, 1, row++);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String entrada = tfEntrada.getText().trim();
            String salida = tfSalida.getText().trim();

            if (cbMecanico.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Debes seleccionar un mecánico.");
                event.consume();
                return;
            }
            if (entrada.isBlank() || salida.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Las horas de entrada y salida son obligatorias.");
                event.consume();
                return;
            }
            if (!entrada.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showAlert(Alert.AlertType.WARNING, "La hora de entrada no es válida. Formato esperado: HH:mm (ej: 08:00).");
                event.consume();
                return;
            }
            if (!salida.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showAlert(Alert.AlertType.WARNING, "La hora de salida no es válida. Formato esperado: HH:mm (ej: 16:00).");
                event.consume();
                return;
            }
            if (entrada.compareTo(salida) >= 0) {
                showAlert(Alert.AlertType.WARNING, "La hora de entrada debe ser anterior a la hora de salida.");
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String mecanicoId = mecanicosCache.stream()
                        .filter(m -> m.get("nombre").equals(cbMecanico.getValue()))
                        .map(m -> m.get("id"))
                        .findFirst().orElse(null);
                Map<String, String> data = new HashMap<>();
                data.put("mecanicoId", mecanicoId);
                data.put("diaSemana", cbDia.getValue());
                data.put("horaEntrada", tfEntrada.getText().trim());
                data.put("horaSalida", tfSalida.getText().trim());
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