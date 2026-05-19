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
import java.util.Map;
import java.util.Optional;

public class ClientesController {

    @FXML private TableView<Map<String, String>> clientesTable;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colNombre;
    @FXML private TableColumn<Map<String, String>, String> colEmail;
    @FXML private TableColumn<Map<String, String>, String> colTelefono;
    @FXML private TableColumn<Map<String, String>, String> colNif;
    @FXML private TableColumn<Map<String, String>, String> colVehiculos;
    @FXML private TableColumn<Map<String, String>, String> colAcciones;
    @FXML private TextField searchField;

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private ObservableList<Map<String, String>> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadClientes();
    }

    private void setupTable() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("id", "")));
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("nombre", "")));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("email", "")));
        colTelefono.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("telefono", "")));
        colNif.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("nif", "")));
        colVehiculos.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrDefault("vehiculos", "")));

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
                clientesTable.setItems(allData);
            } else {
                String lower = newVal.toLowerCase();
                FilteredList<Map<String, String>> filtered = allData.filtered(row ->
                        row.getOrDefault("nombre", "").toLowerCase().contains(lower) ||
                                row.getOrDefault("email", "").toLowerCase().contains(lower) ||
                                row.getOrDefault("nif", "").toLowerCase().contains(lower) ||
                                row.getOrDefault("telefono", "").toLowerCase().contains(lower)
                );
                clientesTable.setItems(filtered);
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
                        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
                        for (JsonNode c : clientes) {
                            Map<String, String> row = new HashMap<>();
                            row.put("id", c.get("id").asText());
                            row.put("nombre", c.path("usuario").path("nombre").asText()
                                    + " " + c.path("usuario").path("apellidos").asText());
                            row.put("email", c.path("usuario").path("email").asText());
                            row.put("telefono", c.path("usuario").path("telefono").asText("-"));
                            row.put("nif", c.path("nif").asText("-"));
                            row.put("vehiculos", "—");
                            data.add(row);
                        }
                        Platform.runLater(() -> {
                            allData = data;
                            clientesTable.setItems(allData);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleNuevoCliente() {
        Dialog<Map<String, String>> dialog = buildFormDialog("Nuevo Cliente", null);
        MainApp.applyDarkDialog(dialog);
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(data);
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/clientes")
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .post(RequestBody.create(json, JSON))
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> {
                                loadClientes();
                                showAlert(Alert.AlertType.INFORMATION, "Cliente creado correctamente.");
                            });
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Error desconocido";
                            JsonNode errorJson = mapper.readTree(errorBody);
                            String msg = errorJson.path("message").asText("Error al crear el cliente.");
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, msg));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEditar(Map<String, String> cliente) {
        if (cliente == null) return;
        Dialog<Map<String, String>> dialog = buildFormDialog("Editar Cliente", cliente);
        MainApp.applyDarkDialog(dialog);
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            new Thread(() -> {
                try {
                    String json = mapper.writeValueAsString(data);
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/admin/clientes/" + cliente.get("id"))
                            .addHeader("Authorization", "Bearer " + AuthService.getToken())
                            .put(RequestBody.create(json, JSON))
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> {
                                loadClientes();
                                showAlert(Alert.AlertType.INFORMATION, "Cliente actualizado correctamente.");
                            });
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al actualizar el cliente."));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void handleEliminar(Map<String, String> cliente) {
        if (cliente == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar cliente " + cliente.get("nombre") + "?");
        confirm.setContentText("Esta acción eliminará también sus vehículos y órdenes asociadas.");
        MainApp.applyDarkDialog(confirm);
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        Request request = new Request.Builder()
                                .url(BASE_URL + "/admin/clientes/" + cliente.get("id"))
                                .addHeader("Authorization", "Bearer " + AuthService.getToken())
                                .delete()
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()) {
                                Platform.runLater(() -> {
                                    loadClientes();
                                    showAlert(Alert.AlertType.INFORMATION, "Cliente eliminado correctamente.");
                                });
                            } else {
                                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error al eliminar el cliente."));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private Dialog<Map<String, String>> buildFormDialog(String titulo, Map<String, String> cliente) {
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

        boolean editando = cliente != null;

        TextField tfNombre = new TextField(editando ? cliente.get("nombre").split(" ")[0] : "");
        TextField tfApellidos = new TextField(editando ? cliente.get("nombre").contains(" ") ?
                cliente.get("nombre").substring(cliente.get("nombre").indexOf(" ") + 1) : "" : "");
        TextField tfEmail = new TextField(editando ? cliente.get("email") : "");
        PasswordField tfPassword = new PasswordField();
        TextField tfTelefono = new TextField(editando ? cliente.getOrDefault("telefono", "") : "");
        TextField tfNif = new TextField(editando ? cliente.getOrDefault("nif", "") : "");

        for (TextField tf : new TextField[]{tfNombre, tfApellidos, tfEmail, tfTelefono, tfNif}) {
            tf.setStyle(fieldStyle);
        }
        tfPassword.setStyle(fieldStyle);

        int row = 0;
        grid.add(new Label("Nombre *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfNombre, 1, row++);
        grid.add(new Label("Apellidos *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfApellidos, 1, row++);
        grid.add(new Label("Email *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfEmail, 1, row++);
        grid.add(new Label(editando ? "Nueva contraseña" : "Contraseña *") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfPassword, 1, row++);
        grid.add(new Label("Teléfono") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfTelefono, 1, row++);
        grid.add(new Label("NIF") {{ setStyle(labelStyle); }}, 0, row);
        grid.add(tfNif, 1, row++);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String nombre = tfNombre.getText().trim();
            String email = tfEmail.getText().trim();
            String password = tfPassword.getText().trim();
            String telefono = tfTelefono.getText().trim();
            String nif = tfNif.getText().trim();

            if (nombre.isBlank() || email.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Los campos marcados con * son obligatorios.");
                event.consume();
                return;
            }
            if (!editando && password.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "La contraseña es obligatoria para nuevos clientes.");
                event.consume();
                return;
            }
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                showAlert(Alert.AlertType.WARNING, "El correo electrónico no es válido.");
                event.consume();
                return;
            }
            if (!editando && password.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "La contraseña debe tener al menos 6 caracteres.");
                event.consume();
                return;
            }
            if (!telefono.isBlank() && !telefono.matches("^[6-9][0-9]{8}$")) {
                showAlert(Alert.AlertType.WARNING, "El teléfono no es válido. Debe tener 9 dígitos y empezar por 6, 7, 8 o 9.");
                event.consume();
                return;
            }
            if (!nif.isBlank()) {
                if (!nif.matches("^[0-9]{8}[A-Za-z]$")) {
                    showAlert(Alert.AlertType.WARNING, "El NIF no es válido. Debe tener 8 dígitos y una letra.");
                    event.consume();
                    return;
                }
                String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
                int numero = Integer.parseInt(nif.substring(0, 8));
                char letraCorrecta = letras.charAt(numero % 23);
                if (Character.toUpperCase(nif.charAt(8)) != letraCorrecta) {
                    showAlert(Alert.AlertType.WARNING, "El NIF no es válido. La letra no corresponde al número.");
                    event.consume();
                    return;
                }
            }
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Map<String, String> data = new HashMap<>();
                data.put("nombre", tfNombre.getText().trim());
                data.put("apellidos", tfApellidos.getText().trim());
                data.put("email", tfEmail.getText().trim());
                data.put("password", tfPassword.getText().trim());
                data.put("telefono", tfTelefono.getText().trim());
                data.put("nif", tfNif.getText().trim());
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