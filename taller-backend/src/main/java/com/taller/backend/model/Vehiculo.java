package com.taller.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(max = 10)
    @Column(nullable = false, unique = true, length = 10)
    private String matricula;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    @Column(nullable = false, columnDefinition = "YEAR")
    private Integer anio;

    @Size(max = 30)
    @Column(length = 30)
    private String color;

    @Size(max = 255)
    @Column(length = 255)
    private String foto;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}