package com.taller.backend.controller;

import com.taller.backend.model.Notificacion;
import com.taller.backend.model.Usuario;
import com.taller.backend.repository.NotificacionRepository;
import com.taller.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionController(NotificacionRepository notificacionRepository,
                                  UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener todas las notificaciones del usuario autenticado
    @GetMapping
    public ResponseEntity<List<Notificacion>> getMisNotificaciones(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(notificacionRepository
                .findByUsuarioIdOrderByFechaDesc(usuario.getId()));
    }

    // Contar notificaciones no leídas
    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        long count = notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Marcar una notificación como leída
    @PatchMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Integer id, Authentication auth) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow();
        notificacion.setLeida(true);
        return ResponseEntity.ok(notificacionRepository.save(notificacion));
    }

    // Marcar todas como leídas
    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        List<Notificacion> noLeidas = notificacionRepository
                .findByUsuarioIdAndLeidaFalse(usuario.getId());
        noLeidas.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(noLeidas);
        return ResponseEntity.noContent().build();
    }
}