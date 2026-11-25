package com.guarderia.logistica_service.persistence.repository;

import com.guarderia.logistica_service.persistence.entity.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {

    Optional<Configuracion> findTopByOrderByIdAsc();
}
