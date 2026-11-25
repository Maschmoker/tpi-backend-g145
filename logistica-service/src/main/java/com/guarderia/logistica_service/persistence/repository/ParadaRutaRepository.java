package com.guarderia.logistica_service.persistence.repository;

import com.guarderia.logistica_service.persistence.entity.ParadaRuta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParadaRutaRepository extends JpaRepository<ParadaRuta, Long> {

    List<ParadaRuta> findByRutaIdOrderByOrdenAsc(Long rutaId);

    Optional<ParadaRuta> findByTrasladoId(Long trasladoId);
}
