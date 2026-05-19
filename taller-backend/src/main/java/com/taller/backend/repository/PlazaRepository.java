package com.taller.backend.repository;

import com.taller.backend.model.Plaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlazaRepository extends JpaRepository<Plaza, Integer> {

    List<Plaza> findByEstado(Plaza.Estado estado);

    Optional<Plaza> findByNumero(String numero);

    boolean existsByNumero(String numero);
}