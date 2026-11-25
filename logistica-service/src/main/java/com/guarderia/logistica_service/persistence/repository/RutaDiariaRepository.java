package com.guarderia.logistica_service.persistence.repository;

import com.guarderia.logistica_service.domain.EstadoRuta;
import com.guarderia.logistica_service.domain.TipoTraslado;
import com.guarderia.logistica_service.persistence.entity.RutaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RutaDiariaRepository extends JpaRepository<RutaDiaria, Long> {

    List<RutaDiaria> findByFecha(LocalDate fecha);

    List<RutaDiaria> findByFechaAndTipo(LocalDate fecha, TipoTraslado tipo);

    Optional<RutaDiaria> findByFechaAndTipoAndChoferKeycloakId(LocalDate fecha, TipoTraslado tipo, String choferKeycloakId);

    List<RutaDiaria> findByFechaAndEstado(LocalDate fecha, EstadoRuta estado);
}
