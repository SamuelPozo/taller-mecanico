package com.taller.backend.repository;

import com.taller.backend.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    List<Horario> findByMecanicoId(Integer mecanicoId);

    boolean existsByMecanicoIdAndDiaSemana(Integer mecanicoId, Horario.DiaSemana diaSemana);
}
