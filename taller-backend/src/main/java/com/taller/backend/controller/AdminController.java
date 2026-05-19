package com.taller.backend.controller;

import com.taller.backend.exception.ResourceNotFoundException;
import com.taller.backend.model.*;
import com.taller.backend.repository.*;
import com.taller.backend.service.OrdenTrabajoService;
import com.taller.backend.service.PlazaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrdenTrabajoService ordenTrabajoService;
    private final PlazaService plazaService;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final MecanicoRepository mecanicoRepository;
    private final ClienteRepository clienteRepository;
    private final HorarioRepository horarioRepository;
    private final AusenciaRepository ausenciaRepository;
    private final NotificacionRepository notificacionRepository;
    private final HistorialEstadoRepository historialEstadoRepository;

    public AdminController(OrdenTrabajoService ordenTrabajoService,
                           PlazaService plazaService,
                           UsuarioRepository usuarioRepository,
                           VehiculoRepository vehiculoRepository,
                           MecanicoRepository mecanicoRepository,
                           ClienteRepository clienteRepository,
                           HorarioRepository horarioRepository,
                           AusenciaRepository ausenciaRepository,
                           NotificacionRepository notificacionRepository,
                           HistorialEstadoRepository historialEstadoRepository) {
        this.ordenTrabajoService = ordenTrabajoService;
        this.plazaService = plazaService;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.mecanicoRepository = mecanicoRepository;
        this.clienteRepository = clienteRepository;
        this.horarioRepository = horarioRepository;
        this.ausenciaRepository = ausenciaRepository;
        this.notificacionRepository = notificacionRepository;
        this.historialEstadoRepository = historialEstadoRepository;
    }

    // ── ÓRDENES DE TRABAJO ───────────────────────────────────────────────────

    @GetMapping("/ordenes")
    public ResponseEntity<List<OrdenTrabajo>> getAllOrdenes() {
        return ResponseEntity.ok(ordenTrabajoService.findAll());
    }

    @GetMapping("/ordenes/{id}")
    public ResponseEntity<OrdenTrabajo> getOrdenById(@PathVariable Integer id) {
        return ResponseEntity.ok(ordenTrabajoService.findById(id));
    }

    @PostMapping("/ordenes")
    public ResponseEntity<OrdenTrabajo> createOrden(@RequestBody Map<String, Object> body,
                                                    Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        Integer vehiculoId = Integer.parseInt(body.get("vehiculoId").toString());
        Integer mecanicoId = Integer.parseInt(body.get("mecanicoId").toString());
        String descripcion = (String) body.getOrDefault("descripcionProblema", "");
        String notas = (String) body.getOrDefault("notasInternas", "");
        return ResponseEntity.ok(ordenTrabajoService.create(vehiculoId, mecanicoId,
                descripcion, notas, usuario));
    }

    @PutMapping("/ordenes/{id}")
    public ResponseEntity<OrdenTrabajo> updateOrden(@PathVariable Integer id,
                                                    @RequestBody Map<String, Object> body) {
        String diagnostico = (String) body.get("diagnostico");
        String notas = (String) body.get("notasInternas");
        String descripcion = (String) body.get("descripcionProblema");
        BigDecimal precioEstimado = body.get("precioEstimado") != null ?
                new BigDecimal(body.get("precioEstimado").toString()) : null;
        BigDecimal precioFinal = body.get("precioFinal") != null ?
                new BigDecimal(body.get("precioFinal").toString()) : null;
        LocalDateTime fechaSalida = body.get("fechaSalidaEstimada") != null ?
                LocalDateTime.parse(body.get("fechaSalidaEstimada").toString()) : null;
        return ResponseEntity.ok(ordenTrabajoService.update(id, diagnostico, notas,
                descripcion, precioEstimado, precioFinal, fechaSalida));
    }

    @PatchMapping("/ordenes/{id}/estado")
    public ResponseEntity<OrdenTrabajo> cambiarEstado(@PathVariable Integer id,
                                                      @RequestBody Map<String, String> body,
                                                      Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        OrdenTrabajo.Estado estado = OrdenTrabajo.Estado.valueOf(body.get("estado"));
        String observacion = body.getOrDefault("observacion", "");
        return ResponseEntity.ok(ordenTrabajoService.cambiarEstado(id, estado,
                observacion, usuario));
    }

    @DeleteMapping("/ordenes/{id}")
    public ResponseEntity<Void> deleteOrden(@PathVariable Integer id) {
        ordenTrabajoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── PLAZAS ───────────────────────────────────────────────────────────────

    @GetMapping("/plazas")
    public ResponseEntity<List<Plaza>> getAllPlazas() {
        return ResponseEntity.ok(plazaService.findAll());
    }

    @GetMapping("/plazas/{id}")
    public ResponseEntity<Plaza> getPlazaById(@PathVariable Integer id) {
        return ResponseEntity.ok(plazaService.findById(id));
    }

    @PostMapping("/plazas")
    public ResponseEntity<Plaza> createPlaza(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(plazaService.create(
                body.get("numero"),
                body.getOrDefault("descripcion", "")));
    }

    @PutMapping("/plazas/{id}")
    public ResponseEntity<Plaza> updatePlaza(@PathVariable Integer id,
                                             @RequestBody Map<String, String> body) {
        Plaza.Estado estado = body.get("estado") != null ?
                Plaza.Estado.valueOf(body.get("estado")) : null;
        return ResponseEntity.ok(plazaService.update(id,
                body.get("descripcion"), estado));
    }

    @DeleteMapping("/plazas/{id}")
    public ResponseEntity<Void> deletePlaza(@PathVariable Integer id) {
        plazaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── VEHÍCULOS ────────────────────────────────────────────────────────────

    @GetMapping("/vehiculos")
    public ResponseEntity<List<com.taller.backend.model.Vehiculo>> getAllVehiculos() {
        return ResponseEntity.ok(vehiculoRepository.findAll());
    }

    @GetMapping("/vehiculos/{id}")
    public ResponseEntity<com.taller.backend.model.Vehiculo> getVehiculoById(@PathVariable Integer id) {
        return ResponseEntity.ok(vehiculoRepository.findById(id)
                .orElseThrow(() -> new com.taller.backend.exception.ResourceNotFoundException("Vehiculo", id)));
    }

    @DeleteMapping("/vehiculos/{id}")
    public ResponseEntity<Void> deleteVehiculo(@PathVariable Integer id) {
        vehiculoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── MECÁNICOS ────────────────────────────────────────────────────────────

    @GetMapping("/mecanicos")
    public ResponseEntity<List<Mecanico>> getAllMecanicos() {
        return ResponseEntity.ok(mecanicoRepository.findAll());
    }

    @GetMapping("/mecanicos/{id}")
    public ResponseEntity<Mecanico> getMecanicoById(@PathVariable Integer id) {
        return ResponseEntity.ok(mecanicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mecanico", id)));
    }

    @DeleteMapping("/mecanicos/{id}")
    public ResponseEntity<Void> deleteMecanico(@PathVariable Integer id) {
        mecanicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── CLIENTES ─────────────────────────────────────────────────────────────

    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> getAllClientes() {
        return ResponseEntity.ok(clienteRepository.findAll());
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id)));
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Integer id) {
        clienteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── HORARIOS ─────────────────────────────────────────────────────────────

    @GetMapping("/horarios")
    public ResponseEntity<List<Horario>> getAllHorarios() {
        return ResponseEntity.ok(horarioRepository.findAll());
    }

    @DeleteMapping("/horarios/{id}")
    public ResponseEntity<Void> deleteHorario(@PathVariable Integer id) {
        horarioRepository.findById(id).ifPresent(horario -> {
            Notificacion n = new Notificacion();
            n.setUsuario(horario.getMecanico().getUsuario());
            n.setTitulo("Horario eliminado");
            n.setMensaje("Tu turno del " + horario.getDiaSemana() +
                    " (" + horario.getHoraEntrada() + " - " + horario.getHoraSalida() +
                    ") ha sido eliminado.");
            n.setTipo(Notificacion.Tipo.GENERAL);
            n.setLeida(false);
            notificacionRepository.save(n);
        });
        horarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── AUSENCIAS ────────────────────────────────────────────────────────────

    @GetMapping("/ausencias")
    public ResponseEntity<List<Ausencia>> getAllAusencias() {
        return ResponseEntity.ok(ausenciaRepository.findAll());
    }

    @PatchMapping("/ausencias/{id}")
    public ResponseEntity<Ausencia> aprobarAusencia(@PathVariable Integer id,
                                                    @RequestBody Map<String, Object> body) {
        Ausencia ausencia = ausenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ausencia", id));
        Boolean aprobada = (Boolean) body.get("aprobada");
        ausencia.setAprobada(aprobada);
        ausenciaRepository.save(ausencia);

        // Notificación al mecánico
        Notificacion n = new Notificacion();
        n.setUsuario(ausencia.getMecanico().getUsuario());
        n.setTitulo(aprobada ? "Ausencia aprobada" : "Ausencia rechazada");
        n.setMensaje(aprobada
                ? "Tu solicitud de ausencia del " + ausencia.getFechaInicio() + " al " + ausencia.getFechaFin() + " ha sido aprobada."
                : "Tu solicitud de ausencia del " + ausencia.getFechaInicio() + " al " + ausencia.getFechaFin() + " ha sido rechazada.");
        n.setTipo(Notificacion.Tipo.AUSENCIA);
        n.setLeida(false);
        notificacionRepository.save(n);

        return ResponseEntity.ok(ausencia);
    }

    // ── ENDPOINTS MECANICO ───────────────────────────────────────────────────

    @GetMapping("/mecanicos/ordenes/{mecanicoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MECANICO')")
    public ResponseEntity<List<OrdenTrabajo>> getOrdenesByMecanico(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(ordenTrabajoService.findByMecanico(mecanicoId));
    }

    @GetMapping("/mecanicos/horarios/{mecanicoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MECANICO')")
    public ResponseEntity<List<Horario>> getHorariosByMecanico(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(horarioRepository.findByMecanicoId(mecanicoId));
    }

    @GetMapping("/mecanicos/ausencias/{mecanicoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MECANICO')")
    public ResponseEntity<List<Ausencia>> getAusenciasByMecanico(@PathVariable Integer mecanicoId) {
        return ResponseEntity.ok(ausenciaRepository.findByMecanicoId(mecanicoId));
    }

    // ── ENDPOINTS CLIENTE ────────────────────────────────────────────────────

    @GetMapping("/clientes/vehiculos/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<List<Vehiculo>> getVehiculosByCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(vehiculoRepository.findByClienteId(clienteId));
    }

    @GetMapping("/clientes/ordenes/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<List<OrdenTrabajo>> getOrdenesByCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(ordenTrabajoService.findByCliente(clienteId));
    }

    // ── CREAR / EDITAR MECÁNICO ──────────────────────────────────────────────

    @PostMapping("/mecanicos")
    public ResponseEntity<Mecanico> createMecanico(@RequestBody Map<String, Object> body) {
        Usuario usuario = new Usuario();
        usuario.setNombre((String) body.get("nombre"));
        usuario.setApellidos((String) body.get("apellidos"));
        usuario.setEmail((String) body.get("email"));
        usuario.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode((String) body.get("password")));
        usuario.setTelefono((String) body.getOrDefault("telefono", ""));
        usuario.setRol(Usuario.Rol.MECANICO);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        Mecanico mecanico = new Mecanico();
        mecanico.setUsuario(usuario);
        mecanico.setEspecialidad((String) body.getOrDefault("especialidad", ""));
        mecanico.setNumEmpleado((String) body.get("numEmpleado"));
        if (body.get("turno") != null)
            mecanico.setTurno(Mecanico.Turno.valueOf((String) body.get("turno")));
        if (body.get("anosExperiencia") != null)
            mecanico.setAnosExperiencia(Integer.parseInt(body.get("anosExperiencia").toString()));
        return ResponseEntity.ok(mecanicoRepository.save(mecanico));
    }

    @PutMapping("/mecanicos/{id}")
    public ResponseEntity<Mecanico> updateMecanico(@PathVariable Integer id,
                                                   @RequestBody Map<String, Object> body) {
        Mecanico mecanico = mecanicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mecanico", id));
        Usuario usuario = mecanico.getUsuario();
        if (body.get("nombre") != null) usuario.setNombre((String) body.get("nombre"));
        if (body.get("apellidos") != null) usuario.setApellidos((String) body.get("apellidos"));
        if (body.get("email") != null) usuario.setEmail((String) body.get("email"));
        if (body.get("telefono") != null) usuario.setTelefono((String) body.get("telefono"));
        if (body.get("password") != null && !((String)body.get("password")).isEmpty())
            usuario.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode((String) body.get("password")));
        usuarioRepository.save(usuario);
        if (body.get("especialidad") != null) mecanico.setEspecialidad((String) body.get("especialidad"));
        if (body.get("numEmpleado") != null) mecanico.setNumEmpleado((String) body.get("numEmpleado"));
        if (body.get("turno") != null) mecanico.setTurno(Mecanico.Turno.valueOf((String) body.get("turno")));
        if (body.get("anosExperiencia") != null) mecanico.setAnosExperiencia(Integer.parseInt(body.get("anosExperiencia").toString()));
        return ResponseEntity.ok(mecanicoRepository.save(mecanico));
    }

    // ── CREAR / EDITAR CLIENTE ───────────────────────────────────────────────

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> createCliente(@RequestBody Map<String, Object> body) {
        Usuario usuario = new Usuario();
        usuario.setNombre((String) body.get("nombre"));
        usuario.setApellidos((String) body.get("apellidos"));
        usuario.setEmail((String) body.get("email"));
        usuario.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .encode((String) body.get("password")));
        usuario.setTelefono((String) body.getOrDefault("telefono", ""));
        usuario.setRol(Usuario.Rol.CLIENTE);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNif((String) body.getOrDefault("nif", ""));
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Integer id,
                                                 @RequestBody Map<String, Object> body) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        Usuario usuario = cliente.getUsuario();
        if (body.get("nombre") != null) usuario.setNombre((String) body.get("nombre"));
        if (body.get("apellidos") != null) usuario.setApellidos((String) body.get("apellidos"));
        if (body.get("email") != null) usuario.setEmail((String) body.get("email"));
        if (body.get("telefono") != null) usuario.setTelefono((String) body.get("telefono"));
        if (body.get("password") != null && !((String)body.get("password")).isEmpty())
            usuario.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode((String) body.get("password")));
        usuarioRepository.save(usuario);
        if (body.get("nif") != null) cliente.setNif((String) body.get("nif"));
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    // ── CREAR / EDITAR VEHÍCULO ──────────────────────────────────────────────

    @PostMapping("/vehiculos")
    public ResponseEntity<Vehiculo> createVehiculo(@RequestBody Map<String, Object> body) {
        Cliente cliente = clienteRepository.findById(Integer.parseInt(body.get("clienteId").toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", 0));
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setCliente(cliente);
        vehiculo.setMatricula((String) body.get("matricula"));
        vehiculo.setMarca((String) body.get("marca"));
        vehiculo.setModelo((String) body.get("modelo"));
        if (body.get("anio") != null && !body.get("anio").toString().isBlank())
            vehiculo.setAnio(Integer.parseInt(body.get("anio").toString()));
        if (body.get("color") != null) vehiculo.setColor((String) body.get("color"));
        return ResponseEntity.ok(vehiculoRepository.save(vehiculo));
    }

    @PutMapping("/vehiculos/{id}")
    public ResponseEntity<Vehiculo> updateVehiculo(@PathVariable Integer id,
                                                   @RequestBody Map<String, Object> body) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", id));
        if (body.get("matricula") != null) vehiculo.setMatricula((String) body.get("matricula"));
        if (body.get("marca") != null) vehiculo.setMarca((String) body.get("marca"));
        if (body.get("modelo") != null) vehiculo.setModelo((String) body.get("modelo"));
        if (body.get("anio") != null && !body.get("anio").toString().isBlank())
            vehiculo.setAnio(Integer.parseInt(body.get("anio").toString()));
        if (body.get("color") != null) vehiculo.setColor((String) body.get("color"));
        return ResponseEntity.ok(vehiculoRepository.save(vehiculo));
    }

    // ── CREAR HORARIO ────────────────────────────────────────────────────────

    @PostMapping("/horarios")
    public ResponseEntity<Horario> createHorario(@RequestBody Map<String, Object> body) {
        Mecanico mecanico = mecanicoRepository.findById(Integer.parseInt(body.get("mecanicoId").toString()))
                .orElseThrow(() -> new ResourceNotFoundException("Mecanico", 0));
        Horario horario = new Horario();
        horario.setMecanico(mecanico);
        horario.setDiaSemana(Horario.DiaSemana.valueOf((String) body.get("diaSemana")));
        horario.setHoraEntrada(java.time.LocalTime.parse((String) body.get("horaEntrada")));
        horario.setHoraSalida(java.time.LocalTime.parse((String) body.get("horaSalida")));
        horarioRepository.save(horario);

        // Notificar al mecánico
        Notificacion n = new Notificacion();
        n.setUsuario(mecanico.getUsuario());
        n.setTitulo("Nuevo horario asignado");
        n.setMensaje("Se te ha asignado un nuevo turno: " + horario.getDiaSemana() +
                " de " + horario.getHoraEntrada() + " a " + horario.getHoraSalida());
        n.setTipo(Notificacion.Tipo.GENERAL);
        n.setLeida(false);
        notificacionRepository.save(n);

        return ResponseEntity.ok(horario);
    }

    @GetMapping("/ordenes/{id}/historial")
    public ResponseEntity<List<HistorialEstado>> getHistorial(@PathVariable Integer id) {
        return ResponseEntity.ok(historialEstadoRepository
                .findByOrdenTrabajoIdOrderByFechaCambioDesc(id));
    }

}

