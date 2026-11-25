package com.guarderia.estadias_service.persistence.repository;

import com.guarderia.estadias_service.persistence.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByEstadiaIdOrderByFechaCambioAsc(Long estadiaId);
}
