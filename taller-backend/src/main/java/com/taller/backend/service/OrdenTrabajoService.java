package com.taller.backend.service;

import com.taller.backend.exception.ResourceNotFoundException;
import com.taller.backend.model.HistorialEstado;
import com.taller.backend.model.Notificacion;
import com.taller.backend.model.OrdenTrabajo;
import com.taller.backend.model.Usuario;
import com.taller.backend.repository.HistorialEstadoRepository;
import com.taller.backend.repository.MecanicoRepository;
import com.taller.backend.repository.NotificacionRepository;
import com.taller.backend.repository.OrdenTrabajoRepository;
import com.taller.backend.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenTrabajoService {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final MecanicoRepository mecanicoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final NotificacionRepository notificacionRepository;

    public OrdenTrabajoService(OrdenTrabajoRepository ordenTrabajoRepository,
                               VehiculoRepository vehiculoRepository,
                               MecanicoRepository mecanicoRepository,
                               HistorialEstadoRepository historialEstadoRepository,
                               NotificacionRepository notificacionRepository) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.mecanicoRepository = mecanicoRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.notificacionRepository = notificacionRepository;
    }

    public List<OrdenTrabajo> findAll() {
        return ordenTrabajoRepository.findAll();
    }

    public OrdenTrabajo findById(Integer id) {
        return ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdenTrabajo", id));
    }

    public List<OrdenTrabajo> findByMecanico(Integer mecanicoId) {
        return ordenTrabajoRepository.findByMecanicoId(mecanicoId);
    }

    public List<OrdenTrabajo> findByEstado(OrdenTrabajo.Estado estado) {
        return ordenTrabajoRepository.findByEstado(estado);
    }

    public List<OrdenTrabajo> findByCliente(Integer clienteId) {
        return ordenTrabajoRepository.findByVehiculoClienteId(clienteId);
    }

    @Transactional
    public OrdenTrabajo create(Integer vehiculoId, Integer mecanicoId,
                               String descripcionProblema, String notasInternas,
                               Usuario usuarioActual) {
        var vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", vehiculoId));
        var mecanico = mecanicoRepository.findById(mecanicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Mecanico", mecanicoId));

        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setVehiculo(vehiculo);
        orden.setMecanico(mecanico);
        orden.setDescripcionProblema(descripcionProblema);
        orden.setNotasInternas(notasInternas);
        orden.setEstado(OrdenTrabajo.Estado.PENDIENTE);
        ordenTrabajoRepository.save(orden);

        HistorialEstado historial = new HistorialEstado();
        historial.setOrdenTrabajo(orden);
        historial.setUsuario(usuarioActual);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo(OrdenTrabajo.Estado.PENDIENTE.name());
        historial.setObservacion("Orden de trabajo creada");
        historialEstadoRepository.save(historial);

        // Notificación al mecánico asignado
        crearNotificacion(
                mecanico.getUsuario(),
                orden,
                "Nueva orden asignada",
                "Se te ha asignado la orden #" + orden.getId() + " — " +
                        vehiculo.getMarca() + " " + vehiculo.getModelo() +
                        " (" + vehiculo.getMatricula() + ")",
                Notificacion.Tipo.GENERAL
        );

        // Notificación al cliente
        crearNotificacion(
                vehiculo.getCliente().getUsuario(),
                orden,
                "Orden de trabajo creada",
                "Tu vehículo " + vehiculo.getMarca() + " " + vehiculo.getModelo() +
                        " (" + vehiculo.getMatricula() + ") ha entrado al taller.",
                Notificacion.Tipo.GENERAL
        );

        return orden;
    }

    @Transactional
    public OrdenTrabajo cambiarEstado(Integer id, OrdenTrabajo.Estado nuevoEstado,
                                      String observacion, Usuario usuarioActual) {
        OrdenTrabajo orden = findById(id);
        String estadoAnterior = orden.getEstado().name();
        orden.setEstado(nuevoEstado);
        ordenTrabajoRepository.save(orden);

        HistorialEstado historial = new HistorialEstado();
        historial.setOrdenTrabajo(orden);
        historial.setUsuario(usuarioActual);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(nuevoEstado.name());
        historial.setObservacion(observacion);
        historialEstadoRepository.save(historial);

        String vehiculoInfo = orden.getVehiculo().getMarca() + " "
                + orden.getVehiculo().getModelo()
                + " (" + orden.getVehiculo().getMatricula() + ")";

        String mensajeEstado = getDescripcionEstado(nuevoEstado);

        // Notificación al cliente
        crearNotificacion(
                orden.getVehiculo().getCliente().getUsuario(),
                orden,
                "Actualización de tu vehículo",
                "Tu " + vehiculoInfo + " — " + mensajeEstado,
                Notificacion.Tipo.ESTADO
        );

        // Notificación al mecánico
        crearNotificacion(
                orden.getMecanico().getUsuario(),
                orden,
                "Estado actualizado — Orden #" + orden.getId(),
                "La orden #" + orden.getId() + " ha cambiado a: " + mensajeEstado,
                Notificacion.Tipo.ESTADO
        );

        return orden;
    }

    @Transactional
    public OrdenTrabajo update(Integer id, String diagnostico, String notasInternas,
                               String descripcionProblema, java.math.BigDecimal precioEstimado,
                               java.math.BigDecimal precioFinal,
                               java.time.LocalDateTime fechaSalidaEstimada) {
        OrdenTrabajo orden = findById(id);

        boolean diagnosticoCambiado = diagnostico != null &&
                !diagnostico.equals(orden.getDiagnostico());

        if (diagnostico != null) orden.setDiagnostico(diagnostico);
        if (notasInternas != null) orden.setNotasInternas(notasInternas);
        if (descripcionProblema != null) orden.setDescripcionProblema(descripcionProblema);
        if (precioEstimado != null) orden.setPrecioEstimado(precioEstimado);
        if (precioFinal != null) orden.setPrecioFinal(precioFinal);
        if (fechaSalidaEstimada != null) orden.setFechaSalidaEstimada(fechaSalidaEstimada);

        ordenTrabajoRepository.save(orden);

        // Guardar en historial si cambió el diagnóstico
        if (diagnosticoCambiado) {
            HistorialEstado historial = new HistorialEstado();
            historial.setOrdenTrabajo(orden);
            historial.setUsuario(orden.getMecanico().getUsuario());
            historial.setEstadoAnterior("DIAGNOSTICO");
            historial.setEstadoNuevo("DIAGNOSTICO");
            historial.setObservacion("Diagnóstico actualizado: " + diagnostico);
            historialEstadoRepository.save(historial);

            // Notificación al cliente
            crearNotificacion(
                    orden.getVehiculo().getCliente().getUsuario(),
                    orden,
                    "Diagnóstico actualizado",
                    "Tu vehículo " + orden.getVehiculo().getMarca() + " " +
                            orden.getVehiculo().getModelo() + " tiene nuevo diagnóstico: " + diagnostico,
                    Notificacion.Tipo.ESTADO
            );
        }

        return orden;
    }

    @Transactional
    public void delete(Integer id) {
        if (!ordenTrabajoRepository.existsById(id)) {
            throw new ResourceNotFoundException("OrdenTrabajo", id);
        }
        ordenTrabajoRepository.deleteById(id);
    }

    private void crearNotificacion(Usuario usuario, OrdenTrabajo orden,
                                   String titulo, String mensaje,
                                   Notificacion.Tipo tipo) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setOrdenTrabajo(orden);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setLeida(false);
        notificacionRepository.save(n);
    }

    private String getDescripcionEstado(OrdenTrabajo.Estado estado) {
        return switch (estado) {
            case PENDIENTE -> "Pendiente de revisión";
            case EN_PROCESO -> "En proceso de reparación";
            case DIAGNOSTICADO -> "Diagnóstico realizado";
            case REPARADO -> "Reparación completada";
            case LISTO -> "Listo para recoger";
            case ENTREGADO -> "Vehículo entregado";
        };
    }
}