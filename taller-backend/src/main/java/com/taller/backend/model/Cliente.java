package com.taller.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El usuario es obligatorio")
    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Size(max = 255)
    @Column(length = 255)
    private String direccion;

    @Size(max = 20)
    @Column(unique = true, length = 20)
    private String nif;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferencias_contacto", length = 10)
    private PreferenciaContacto preferenciasContacto = PreferenciaContacto.APP;

    public enum PreferenciaContacto {
        EMAIL, TELEFONO, APP
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public PreferenciaContacto getPreferenciasContacto() { return preferenciasContacto; }
    public void setPreferenciasContacto(PreferenciaContacto preferenciasContacto) { this.preferenciasContacto = preferenciasContacto; }
}