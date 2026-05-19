package com.taller.backend.controller;

import com.taller.backend.model.*;
import com.taller.backend.repository.*;
import com.taller.backend.service.OrdenTrabajoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mecanico")
@PreAuthorize("hasRole('MECANICO') or hasRole('ADMIN')")
public class MecanicoController {

    private final OrdenTrabajoService ordenTrabajoService;
    private final HorarioRepository horarioRepository;
    private final AusenciaRepository ausenciaRepository;
    private final MecanicoRepository mecanicoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    public MecanicoController(OrdenTrabajoService ordenTrabajoService,
                              HorarioRepository horarioRepository,
                              AusenciaRepository ausenciaRepository,
                              MecanicoRepository mecanicoRepository,
                              HistorialEstadoRepository historialEstadoRepository,
                              UsuarioRepository usuarioRepository,
                              NotificacionRepository notificacionRepository) {
        this.ordenTrabajoService = ordenTrabajoService;
        this.horarioRepository = horarioRepository;
        this.ausenciaRepository = ausenciaRepository;
        this.mecanicoRepository = mecanicoRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @GetMapping("/ordenes/{mecanicoId}")
    public ResponseEntity<List<OrdenTrabajo>> getOrdenes(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(ordenTrabajoService.findByMecanico(mecanicoId));
    }

    @GetMapping("/horarios/{mecanicoId}")
    public ResponseEntity<List<Horario>> getHorarios(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(horarioRepository.findByMecanicoId(mecanicoId));
    }

    @GetMapping("/ausencias/{mecanicoId}")
    public ResponseEntity<List<Ausencia>> getAusencias(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(ausenciaRepository.findByMecanicoId(mecanicoId));
    }

    @PostMapping("/ausencias")
    public ResponseEntity<Ausencia> createAusencia(@RequestBody Map<String, Object> body) {
        Mecanico mecanico = mecanicoRepository.findById(
                        Integer.parseInt(body.get("mecanicoId").toString()))
                .orElseThrow(() -> new RuntimeException("Mecánico no encontrado"));
        Ausencia ausencia = new Ausencia();
        ausencia.setMecanico(mecanico);
        ausencia.setTipo(Ausencia.Tipo.valueOf((String) body.get("tipo")));
        ausencia.setFechaInicio(java.time.LocalDate.parse((String) body.get("fechaInicio")));
        ausencia.setFechaFin(java.time.LocalDate.parse((String) body.get("fechaFin")));
        ausencia.setMotivo((String) body.getOrDefault("motivo", ""));
        ausencia.setAprobada(null);
        ausenciaRepository.save(ausencia);

        // Notificar a todos los administradores
        usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.ADMIN)
                .forEach(admin -> {
                    Notificacion n = new Notificacion();
                    n.setUsuario(admin);
                    n.setTitulo("Nueva solicitud de ausencia");
                    n.setMensaje(mecanico.getUsuario().getNombre() + " " +
                            mecanico.getUsuario().getApellidos() +
                                    " ha solicitado una ausencia del " +
                                    formatearFecha(ausencia.getFechaInicio()) + " al " + formatearFecha(ausencia.getFechaFin()) +
                            " (" + ausencia.getTipo() + ")");
                    n.setTipo(Notificacion.Tipo.AUSENCIA);
                    n.setLeida(false);
                    notificacionRepository.save(n);
                });

        return ResponseEntity.ok(ausencia);
    }

    @GetMapping("/ordenes/{id}/historial")
    public ResponseEntity<List<HistorialEstado>> getHistorial(@PathVariable Integer id) {
        return ResponseEntity.ok(historialEstadoRepository
                .findByOrdenTrabajoIdOrderByFechaCambioDesc(id));
    }

    @PatchMapping("/ordenes/{id}/estado")
    public ResponseEntity<OrdenTrabajo> cambiarEstado(@PathVariable Integer id,
                                                      @RequestBody Map<String, Object> body,
                                                      Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        OrdenTrabajo.Estado estado = OrdenTrabajo.Estado.valueOf((String) body.get("estado"));
        String observacion = (String) body.getOrDefault("observacion", "");
        return ResponseEntity.ok(ordenTrabajoService.cambiarEstado(id, estado, observacion, usuario));
    }

    @PatchMapping("/ordenes/{id}/diagnostico")
    public ResponseEntity<OrdenTrabajo> actualizarDiagnostico(@PathVariable Integer id,
                                                              @RequestBody Map<String, Object> body) {
        String diagnostico = (String) body.get("diagnostico");
        String precioEstimado = body.get("precioEstimado") != null ?
                body.get("precioEstimado").toString() : null;
        return ResponseEntity.ok(ordenTrabajoService.update(id, diagnostico, null, null,
                precioEstimado != null ? new java.math.BigDecimal(precioEstimado) : null,
                null, null));
    }

    private String formatearFecha(java.time.LocalDate fecha) {
        return fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}