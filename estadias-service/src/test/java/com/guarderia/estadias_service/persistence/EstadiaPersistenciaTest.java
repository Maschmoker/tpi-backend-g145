package com.guarderia.estadias_service.persistence;

import com.guarderia.estadias_service.domain.EstadoEstadia;
import com.guarderia.estadias_service.domain.EstadoServicioCuidado;
import com.guarderia.estadias_service.domain.ModalidadEstadia;
import com.guarderia.estadias_service.persistence.entity.Estadia;
import com.guarderia.estadias_service.persistence.entity.EstadiaServicio;
import com.guarderia.estadias_service.persistence.entity.ServicioCatalogo;
import com.guarderia.estadias_service.persistence.repository.EstadiaRepository;
import com.guarderia.estadias_service.persistence.repository.EstadiaServicioRepository;
import com.guarderia.estadias_service.persistence.repository.ServicioCatalogoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class EstadiaPersistenciaTest {

    @Autowired
    private ServicioCatalogoRepository servicioCatalogoRepository;

    @Autowired
    private EstadiaRepository estadiaRepository;

    @Autowired
    private EstadiaServicioRepository estadiaServicioRepository;

    @Test
    void mapeoHibernateValidaEsquemaFlyway() {
        assertThat(servicioCatalogoRepository.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void guardaYRecuperaEstadiaConServicios() {
        ServicioCatalogo servicio = servicioCatalogoRepository.findByActivoTrue().get(0);

        Estadia estadia = new Estadia();
        estadia.setClienteId(1L);
        estadia.setClienteKeycloakId("kc-cliente-1");
        estadia.setMascotaId(10L);
        estadia.setMascotaNombreSnapshot("Firulais");
        estadia.setMascotaEspecieSnapshot("PERRO");
        estadia.setMascotaTamanioSnapshot("MEDIANO");
        estadia.setModalidad(ModalidadEstadia.PRESENCIAL);
        estadia.setFechaIngresoPlanificada(LocalDate.now().plusDays(1));
        estadia.setFechaEgresoPlanificada(LocalDate.now().plusDays(3));
        estadia = estadiaRepository.saveAndFlush(estadia);

        EstadiaServicio es = new EstadiaServicio();
        es.setEstadia(estadia);
        es.setServicioCatalogo(servicio);
        es.setPrecioAplicado(servicio.getPrecioMediano());
        es.setEstado(EstadoServicioCuidado.PENDIENTE);
        estadiaServicioRepository.saveAndFlush(es);

        Estadia recuperada = estadiaRepository.findById(estadia.getId()).orElseThrow();
        assertThat(estadiaServicioRepository.findByEstadiaId(recuperada.getId())).hasSize(1);
        assertThat(recuperada.getEstado()).isEqualTo(EstadoEstadia.SOLICITADA);
    }

    @Test
    void validaEstadiaActivaPorMascota() {
        Estadia estadia = new Estadia();
        estadia.setClienteId(2L);
        estadia.setClienteKeycloakId("kc-cliente-2");
        estadia.setMascotaId(20L);
        estadia.setMascotaNombreSnapshot("Mishi");
        estadia.setMascotaEspecieSnapshot("GATO");
        estadia.setMascotaTamanioSnapshot("PEQUENO");
        estadia.setModalidad(ModalidadEstadia.PRESENCIAL);
        estadia.setFechaIngresoPlanificada(LocalDate.now().plusDays(1));
        estadia.setFechaEgresoPlanificada(LocalDate.now().plusDays(2));
        estadiaRepository.saveAndFlush(estadia);

        List<EstadoEstadia> terminales = List.of(EstadoEstadia.ENTREGADA, EstadoEstadia.CANCELADA);
        assertThat(estadiaRepository.existsByMascotaIdAndEstadoNotIn(20L, terminales)).isTrue();
        assertThat(estadiaRepository.existsByMascotaIdAndEstadoNotIn(999L, terminales)).isFalse();
    }

    @Test
    void todosLosServiciosRealizados() {
        Estadia estadia = new Estadia();
        estadia.setClienteId(3L);
        estadia.setClienteKeycloakId("kc-cliente-3");
        estadia.setMascotaId(30L);
        estadia.setMascotaNombreSnapshot("Rex");
        estadia.setMascotaEspecieSnapshot("PERRO");
        estadia.setMascotaTamanioSnapshot("GRANDE");
        estadia.setModalidad(ModalidadEstadia.PRESENCIAL);
        estadia.setFechaIngresoPlanificada(LocalDate.now().plusDays(1));
        estadia.setFechaEgresoPlanificada(LocalDate.now().plusDays(2));
        estadia = estadiaRepository.saveAndFlush(estadia);

        ServicioCatalogo servicio = servicioCatalogoRepository.findByActivoTrue().get(0);
        EstadiaServicio es = new EstadiaServicio();
        es.setEstadia(estadia);
        es.setServicioCatalogo(servicio);
        es.setPrecioAplicado(servicio.getPrecioGrande());
        es.setEstado(EstadoServicioCuidado.REALIZADO);
        estadiaServicioRepository.saveAndFlush(es);

        long pendientes = estadiaServicioRepository.countByEstadiaIdAndEstadoNot(estadia.getId(), EstadoServicioCuidado.REALIZADO);
        assertThat(pendientes).isZero();
    }
}
