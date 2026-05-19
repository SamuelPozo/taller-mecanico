package com.taller.backend.repository;

import com.taller.backend.model.Ausencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AusenciaRepository extends JpaRepository<Ausencia, Integer> {

    List<Ausencia> findByMecanicoId(Integer mecanicoId);

    List<Ausencia> findByAprobadaIsNull();

    List<Ausencia> findByMecanicoIdAndAprobadaIsNull(Integer mecanicoId);
}