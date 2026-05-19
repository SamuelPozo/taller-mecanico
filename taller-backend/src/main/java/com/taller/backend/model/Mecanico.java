package com.taller.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "mecanico")
public class Mecanico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El usuario es obligatorio")
    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Size(max = 100)
    @Column(length = 100)
    private String especialidad;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Turno turno;

    @Min(0)
    @Column(name = "anos_experiencia")
    private Integer anosExperiencia = 0;

    @NotBlank(message = "El número de empleado es obligatorio")
    @Size(max = 20)
    @Column(name = "num_empleado", nullable = false, unique = true, length = 20)
    private String numEmpleado;

    public enum Turno {
        MANANA, TARDE, NOCHE
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public Integer getAnosExperiencia() { return anosExperiencia; }
    public void setAnosExperiencia(Integer anosExperiencia) { this.anosExperiencia = anosExperiencia; }

    public String getNumEmpleado() { return numEmpleado; }
    public void setNumEmpleado(String numEmpleado) { this.numEmpleado = numEmpleado; }
}