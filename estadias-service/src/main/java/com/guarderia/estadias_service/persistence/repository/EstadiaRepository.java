package com.guarderia.estadias_service.persistence.repository;

import com.guarderia.estadias_service.domain.EstadoEstadia;
import com.guarderia.estadias_service.persistence.entity.Estadia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EstadiaRepository extends JpaRepository<Estadia, Long> {

    List<Estadia> findByClienteId(Long clienteId);

    List<Estadia> findByClienteKeycloakId(String clienteKeycloakId);

    List<Estadia> findByMascotaId(Long mascotaId);

    Optional<Estadia> findByIdAndClienteKeycloakId(Long id, String clienteKeycloakId);

    List<Estadia> findByEstado(EstadoEstadia estado);

    List<Estadia> findByFechaIngresoPlanificadaBetween(LocalDate desde, LocalDate hasta);

    boolean existsByMascotaIdAndEstadoNotIn(Long mascotaId, Collection<EstadoEstadia> estadosTerminales);

    boolean existsByMascotaIdAndEstadoIn(Long mascotaId, Collection<EstadoEstadia> estadosActivos);
}
