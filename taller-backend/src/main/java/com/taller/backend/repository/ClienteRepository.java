package com.taller.backend.repository;

import com.taller.backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByUsuarioId(Integer usuarioId);

    Optional<Cliente> findByNif(String nif);

    boolean existsByNif(String nif);
}