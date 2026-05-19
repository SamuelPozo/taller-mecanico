package com.taller.backend.repository;

import com.taller.backend.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    List<Vehiculo> findByClienteId(Integer clienteId);

    Optional<Vehiculo> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);
}