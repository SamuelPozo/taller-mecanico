package com.taller.backend.repository;

import com.taller.backend.model.Estacionamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstacionamientoRepository extends JpaRepository<Estacionamiento, Integer> {

    List<Estacionamiento> findByActivoTrue();

    Optional<Estacionamiento> findByOrdenTrabajoId(Integer ordenTrabajoId);

    List<Estacionamiento> findByMecanicoId(Integer mecanicoId);

    List<Estacionamiento> findByClienteId(Integer clienteId);

    Optional<Estacionamiento> findByPlazaIdAndActivoTrue(Integer plazaId);
}