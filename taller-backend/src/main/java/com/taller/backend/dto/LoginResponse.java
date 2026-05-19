package com.taller.backend.dto;

public record LoginResponse(
        String token,
        String email,
        String nombre,
        String apellidos,
        String rol,
        Integer id,
        Integer mecanicoId,
        Integer clienteId
) {}