package com.taller.backend.repository;

import com.taller.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<Usuario> findByRol(Usuario.Rol rol);

    boolean existsByEmailAndIdNot(String email, Integer id);

    boolean existsByRol(Usuario.Rol rol);
}