package com.taller.backend.repository;

import com.taller.backend.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Integer> {

    List<HistorialEstado> findByOrdenTrabajoIdOrderByFechaCambioDesc(Integer ordenTrabajoId);
}