package com.guarderia.estadias_service.persistence.repository;

import com.guarderia.estadias_service.domain.EstadoServicioCuidado;
import com.guarderia.estadias_service.persistence.entity.EstadiaServicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadiaServicioRepository extends JpaRepository<EstadiaServicio, Long> {

    // El mapeo a DTO ocurre en el controlador, fuera de la transaccion
    // (open-in-view=false): se inicializa servicioCatalogo junto con el item.
    @Override
    @EntityGraph(attributePaths = {"servicioCatalogo"})
    Optional<EstadiaServicio> findById(Long id);

    @EntityGraph(attributePaths = {"servicioCatalogo"})
    List<EstadiaServicio> findByEstadiaId(Long estadiaId);

    long countByEstadiaIdAndEstado(Long estadiaId, EstadoServicioCuidado estado);

    long countByEstadiaIdAndEstadoNot(Long estadiaId, EstadoServicioCuidado estado);
}
