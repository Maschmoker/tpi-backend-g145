package com.guarderia.estadias_service.persistence.repository;

import com.guarderia.estadias_service.domain.EstadoServicioCuidado;
import com.guarderia.estadias_service.persistence.entity.EstadiaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstadiaServicioRepository extends JpaRepository<EstadiaServicio, Long> {

    List<EstadiaServicio> findByEstadiaId(Long estadiaId);

    long countByEstadiaIdAndEstado(Long estadiaId, EstadoServicioCuidado estado);

    long countByEstadiaIdAndEstadoNot(Long estadiaId, EstadoServicioCuidado estado);
}
