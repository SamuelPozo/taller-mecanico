package com.taller.backend.controller;

import com.taller.backend.model.HistorialEstado;
import com.taller.backend.model.OrdenTrabajo;
import com.taller.backend.model.Vehiculo;
import com.taller.backend.repository.HistorialEstadoRepository;
import com.taller.backend.repository.VehiculoRepository;
import com.taller.backend.service.OrdenTrabajoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente")
@PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
public class ClienteController {

    private final VehiculoRepository vehiculoRepository;
    private final OrdenTrabajoService ordenTrabajoService;
    private final HistorialEstadoRepository historialEstadoRepository;

    public ClienteController(VehiculoRepository vehiculoRepository,
                             OrdenTrabajoService ordenTrabajoService,
                             HistorialEstadoRepository historialEstadoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.ordenTrabajoService = ordenTrabajoService;
        this.historialEstadoRepository = historialEstadoRepository;
    }

    @GetMapping("/vehiculos/{clienteId}")
    public ResponseEntity<List<Vehiculo>> getVehiculos(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(vehiculoRepository.findByClienteId(clienteId));
    }

    @GetMapping("/ordenes/{clienteId}")
    public ResponseEntity<List<OrdenTrabajo>> getOrdenes(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(ordenTrabajoService.findByCliente(clienteId));
    }

    @GetMapping("/ordenes/{id}/historial")
    public ResponseEntity<List<HistorialEstado>> getHistorial(@PathVariable Integer id) {
        return ResponseEntity.ok(historialEstadoRepository
                .findByOrdenTrabajoIdOrderByFechaCambioDesc(id));
    }
}