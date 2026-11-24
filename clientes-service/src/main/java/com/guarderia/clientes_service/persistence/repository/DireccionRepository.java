package com.guarderia.clientes_service.persistence.repository;

import com.guarderia.clientes_service.persistence.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByClienteId(Long clienteId);

    List<Direccion> findByCliente_KeycloakId(String keycloakId);

    Optional<Direccion> findByIdAndCliente_KeycloakId(Long id, String keycloakId);

    Optional<Direccion> findByClienteIdAndEsPrincipalTrue(Long clienteId);

    boolean existsByClienteIdAndEsPrincipalTrue(Long clienteId);
}