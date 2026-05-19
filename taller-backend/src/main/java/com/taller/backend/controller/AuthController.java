package com.taller.backend.controller;

import com.taller.backend.dto.LoginRequest;
import com.taller.backend.dto.LoginResponse;
import com.taller.backend.dto.RegisterRequest;
import com.taller.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final com.taller.backend.repository.UsuarioRepository usuarioRepository;

    public AuthController(AuthService authService,
                          com.taller.backend.repository.UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PatchMapping("/foto-perfil")
    public ResponseEntity<Void> actualizarFotoPerfil(@RequestBody java.util.Map<String, String> body,
                                                     org.springframework.security.core.Authentication auth) {
        String fotoPerfil = body.get("fotoPerfil");
        com.taller.backend.model.Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        usuario.setFotoPerfil(fotoPerfil);
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }
}