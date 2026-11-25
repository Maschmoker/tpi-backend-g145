package com.guarderia.logistica_service.persistence.repository;

import com.guarderia.logistica_service.domain.EstadoTraslado;
import com.guarderia.logistica_service.domain.TipoTraslado;
import com.guarderia.logistica_service.persistence.entity.Traslado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrasladoRepository extends JpaRepository<Traslado, Long> {

    List<Traslado> findByFechaProgramada(LocalDate fecha);

    List<Traslado> findByFechaProgramadaAndTipo(LocalDate fecha, TipoTraslado tipo);

    List<Traslado> findByFechaProgramadaAndEstadoIn(LocalDate fecha, Collection<EstadoTraslado> estados);

    List<Traslado> findByFechaProgramadaAndTipoAndEstadoIn(LocalDate fecha, TipoTraslado tipo, Collection<EstadoTraslado> estados);

    long countByFechaProgramadaAndEstadoIn(LocalDate fecha, Collection<EstadoTraslado> estados);

    long countByFechaProgramadaAndEstadoNotIn(LocalDate fecha, Collection<EstadoTraslado> estadosExcluidos);

    List<Traslado> findByEstadiaId(Long estadiaId);

    Optional<Traslado> findByEstadiaIdAndTipo(Long estadiaId, TipoTraslado tipo);

    List<Traslado> findByEstado(EstadoTraslado estado);
}
