package com.guarderia.estadias_service.persistence.repository;

import com.guarderia.estadias_service.domain.TipoServicio;
import com.guarderia.estadias_service.persistence.entity.ServicioCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioCatalogoRepository extends JpaRepository<ServicioCatalogo, Long> {

    List<ServicioCatalogo> findByActivoTrue();

    List<ServicioCatalogo> findByTipoServicioAndActivoTrue(TipoServicio tipoServicio);

    List<ServicioCatalogo> findByActivoTrueAndEspeciesAplicablesContaining(String especie);
}
