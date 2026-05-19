package com.taller.backend.repository;

import com.taller.backend.model.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Integer> {

    List<OrdenTrabajo> findByMecanicoId(Integer mecanicoId);

    List<OrdenTrabajo> findByVehiculoId(Integer vehiculoId);

    List<OrdenTrabajo> findByEstado(OrdenTrabajo.Estado estado);

    List<OrdenTrabajo> findByVehiculoClienteId(Integer clienteId);
}