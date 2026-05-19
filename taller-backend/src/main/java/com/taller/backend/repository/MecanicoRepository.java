package com.taller.backend.repository;

import com.taller.backend.model.Mecanico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface MecanicoRepository extends JpaRepository<Mecanico, Integer> {

    Optional<Mecanico> findByUsuarioId(Integer usuarioId);

    boolean existsByNumEmpleado(String numEmpleado);

    Optional<Mecanico> findByNumEmpleado(String numEmpleado);
}