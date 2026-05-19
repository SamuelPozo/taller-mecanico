package com.taller.backend.controller;

import com.taller.backend.model.Usuario;
import com.taller.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/setup")
public class SetupController {

    private final UsuarioRepository usuarioRepository;

    public SetupController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<?> setup(@RequestBody Map<String, Object> body) {
        boolean adminExiste = usuarioRepository.existsByRol(Usuario.Rol.ADMIN);
        if (adminExiste) {
            return ResponseEntity.status(403).body(
                    Map.of("message", "Ya existe un administrador en el sistema.")
            );
        }

        Usuario usuario = new Usuario();
        usuario.setNombre((String) body.get("nombre"));
        usuario.setApellidos((String) body.get("apellidos"));
        usuario.setEmail((String) body.get("email"));
        usuario.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode((String) body.get("password")));
        usuario.setTelefono((String) body.getOrDefault("telefono", ""));
        usuario.setRol(Usuario.Rol.ADMIN);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Administrador creado correctamente."));
    }
}