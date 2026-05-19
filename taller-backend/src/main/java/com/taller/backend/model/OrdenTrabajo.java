package com.taller.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orden_trabajo")
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El vehículo es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @NotNull(message = "El mecánico es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_mecanico", nullable = false)
    private Mecanico mecanico;

    @Column(name = "fecha_entrada", nullable = false, updatable = false)
    private LocalDateTime fechaEntrada;

    @Column(name = "fecha_salida_estimada")
    private LocalDateTime fechaSalidaEstimada;

    @Column(name = "fecha_salida_real")
    private LocalDateTime fechaSalidaReal;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado = Estado.PENDIENTE;

    @Column(name = "descripcion_problema", columnDefinition = "TEXT")
    private String descripcionProblema;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @DecimalMin(value = "0.0", message = "El precio estimado no puede ser negativo")
    @Column(name = "precio_estimado", precision = 10, scale = 2)
    private BigDecimal precioEstimado;

    @DecimalMin(value = "0.0", message = "El precio final no puede ser negativo")
    @Column(name = "precio_final", precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(name = "notas_internas", columnDefinition = "TEXT")
    private String notasInternas;

    @PrePersist
    protected void onCreate() {
        this.fechaEntrada = LocalDateTime.now();
    }

    public enum Estado {
        PENDIENTE, EN_PROCESO, DIAGNOSTICADO, REPARADO, LISTO, ENTREGADO
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public Mecanico getMecanico() { return mecanico; }
    public void setMecanico(Mecanico mecanico) { this.mecanico = mecanico; }

    public LocalDateTime getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDateTime fechaEntrada) { this.fechaEntrada = fechaEntrada; }

    public LocalDateTime getFechaSalidaEstimada() { return fechaSalidaEstimada; }
    public void setFechaSalidaEstimada(LocalDateTime fechaSalidaEstimada) { this.fechaSalidaEstimada = fechaSalidaEstimada; }

    public LocalDateTime getFechaSalidaReal() { return fechaSalidaReal; }
    public void setFechaSalidaReal(LocalDateTime fechaSalidaReal) { this.fechaSalidaReal = fechaSalidaReal; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public String getDescripcionProblema() { return descripcionProblema; }
    public void setDescripcionProblema(String descripcionProblema) { this.descripcionProblema = descripcionProblema; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public BigDecimal getPrecioEstimado() { return precioEstimado; }
    public void setPrecioEstimado(BigDecimal precioEstimado) { this.precioEstimado = precioEstimado; }

    public BigDecimal getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(BigDecimal precioFinal) { this.precioFinal = precioFinal; }

    public String getNotasInternas() { return notasInternas; }
    public void setNotasInternas(String notasInternas) { this.notasInternas = notasInternas; }
}