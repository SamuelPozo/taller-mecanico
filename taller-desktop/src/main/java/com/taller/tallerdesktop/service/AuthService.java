package com.taller.tallerdesktop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

public class AuthService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String token;
    private static String userEmail;
    private static String userName;
    private static String userRole;
    private static Integer userId;

    public String login(String email, String password) throws Exception {
        String json = mapper.writeValueAsString(new java.util.HashMap<>() {{
            put("email", email);
            put("password", password);
        }});

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/login")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (response.isSuccessful()) {
                JsonNode node = mapper.readTree(responseBody);
                String rol = node.get("rol").asText();
                if (!rol.equals("ADMIN")) {
                    return "Esta aplicación es solo para administradores. Usa la app móvil.";
                }
                token = node.get("token").asText();
                userEmail = node.get("email").asText();
                userName = node.get("nombre").asText() + " " + node.get("apellidos").asText();
                userRole = rol;
                userId = node.get("id").asInt();
                return null; // null = éxito
            }

            // Intentar leer el mensaje de error del backend
            try {
                JsonNode errorNode = mapper.readTree(responseBody);
                String msg = errorNode.path("message").asText("");
                if (!msg.isBlank()) return msg;
            } catch (Exception ignored) {}

            return switch (response.code()) {
                case 401 -> "Credenciales incorrectas. Comprueba tu email y contraseña.";
                case 403 -> "No tienes permiso para acceder a esta aplicación.";
                case 404 -> "Usuario no encontrado.";
                case 500 -> "Error del servidor. Inténtalo de nuevo más tarde.";
                default -> "Error al iniciar sesión. Código: " + response.code();
            };
        }
    }

    public static String getToken() { return token; }
    public static String getUserEmail() { return userEmail; }
    public static String getUserName() { return userName; }
    public static String getUserRole() { return userRole; }
    public static Integer getUserId() { return userId; }

    public static void logout() {
        token = null;
        userEmail = null;
        userName = null;
        userRole = null;
        userId = null;
    }
}