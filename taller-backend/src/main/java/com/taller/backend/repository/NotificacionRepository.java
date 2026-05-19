package com.taller.backend.repository;

import com.taller.backend.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Integer usuarioId);

    List<Notificacion> findByUsuarioIdAndLeidaFalse(Integer usuarioId);

    long countByUsuarioIdAndLeidaFalse(Integer usuarioId);
}