package com.guarderia.clientes_service.persistence.repository;

import com.guarderia.clientes_service.persistence.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByClienteId(Long clienteId);

    List<Mascota> findByCliente_KeycloakId(String keycloakId);

    Optional<Mascota> findByIdAndCliente_KeycloakId(Long id, String keycloakId);

    List<Mascota> findByCliente_KeycloakIdAndActivaTrue(String keycloakId);

    boolean existsByIdAndCliente_KeycloakIdAndActivaTrue(Long id, String keycloakId);

    long countByClienteIdAndActivaTrue(Long clienteId);
}