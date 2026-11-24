package com.guarderia.clientes_service.persistence.repository;

import com.guarderia.clientes_service.persistence.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByKeycloakId(String keycloakId);

    boolean existsByKeycloakId(String keycloakId);

    Optional<Cliente> findByIdAndKeycloakId(Long id, String keycloakId);

    List<Cliente> findByActivoTrue();
}