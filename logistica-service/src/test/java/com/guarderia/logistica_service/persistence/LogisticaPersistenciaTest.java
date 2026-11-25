package com.guarderia.logistica_service.persistence;

import com.guarderia.logistica_service.domain.EstadoTraslado;
import com.guarderia.logistica_service.domain.TipoTraslado;
import com.guarderia.logistica_service.persistence.entity.Configuracion;
import com.guarderia.logistica_service.persistence.entity.ParadaRuta;
import com.guarderia.logistica_service.persistence.entity.RutaDiaria;
import com.guarderia.logistica_service.persistence.entity.Traslado;
import com.guarderia.logistica_service.persistence.repository.ConfiguracionRepository;
import com.guarderia.logistica_service.persistence.repository.ParadaRutaRepository;
import com.guarderia.logistica_service.persistence.repository.RutaDiariaRepository;
import com.guarderia.logistica_service.persistence.repository.TrasladoRepository;
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
class LogisticaPersistenciaTest {

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Autowired
    private TrasladoRepository trasladoRepository;

    @Autowired
    private RutaDiariaRepository rutaDiariaRepository;

    @Autowired
    private ParadaRutaRepository paradaRutaRepository;

    @Test
    void mapeoHibernateValidaEsquemaFlyway() {
        assertThat(configuracionRepository.count()).isGreaterThanOrEqualTo(1);
        Configuracion cfg = configuracionRepository.findTopByOrderByIdAsc().orElseThrow();
        assertThat(cfg.getGuarderiaLatitud()).isEqualTo(-31.4201);
        assertThat(cfg.getCapacidadDiariaTraslados()).isEqualTo(15);
    }

    @Test
    void guardaYRecuperaTraslado() {
        Traslado t = new Traslado();
        t.setEstadiaId(100L);
        t.setClienteId(1L);
        t.setDireccionId(10L);
        t.setTipo(TipoTraslado.RETIRO);
        t.setOrigenLatitud(-31.4201);
        t.setOrigenLongitud(-64.1888);
        t.setDestinoLatitud(-31.4);
        t.setDestinoLongitud(-64.2);
        t.setFechaProgramada(LocalDate.now().plusDays(1));
        t.setEstimado(false);
        t = trasladoRepository.saveAndFlush(t);

        Traslado recuperado = trasladoRepository.findById(t.getId()).orElseThrow();
        assertThat(recuperado.getEstado()).isEqualTo(EstadoTraslado.PENDIENTE);
        assertThat(recuperado.getTipo()).isEqualTo(TipoTraslado.RETIRO);
        assertThat(trasladoRepository.findByEstadiaId(100L)).hasSize(1);
        assertThat(trasladoRepository.findByEstadiaIdAndTipo(100L, TipoTraslado.RETIRO)).isPresent();
    }

    @Test
    void cuentaTrasladosActivosPorFecha() {
        LocalDate fecha = LocalDate.now().plusDays(2);

        Traslado t1 = new Traslado();
        t1.setEstadiaId(200L);
        t1.setClienteId(2L);
        t1.setDireccionId(20L);
        t1.setTipo(TipoTraslado.DEVOLUCION);
        t1.setOrigenLatitud(-31.4);
        t1.setOrigenLongitud(-64.2);
        t1.setDestinoLatitud(-31.4201);
        t1.setDestinoLongitud(-64.1888);
        t1.setFechaProgramada(fecha);
        trasladoRepository.saveAndFlush(t1);

        long activos = trasladoRepository.countByFechaProgramadaAndEstadoNotIn(
                fecha, List.of(EstadoTraslado.CANCELADO, EstadoTraslado.COMPLETADO));
        assertThat(activos).isGreaterThanOrEqualTo(1);
    }

    @Test
    void buscaTrasladosPorEstadia() {
        Traslado t = new Traslado();
        t.setEstadiaId(400L);
        t.setClienteId(4L);
        t.setDireccionId(40L);
        t.setTipo(TipoTraslado.RETIRO);
        t.setOrigenLatitud(-31.4201);
        t.setOrigenLongitud(-64.1888);
        t.setDestinoLatitud(-31.4);
        t.setDestinoLongitud(-64.2);
        t.setFechaProgramada(LocalDate.now().plusDays(4));
        trasladoRepository.saveAndFlush(t);

        assertThat(trasladoRepository.findByEstadiaId(400L)).hasSize(1);
        assertThat(trasladoRepository.findByEstadiaIdAndTipo(400L, TipoTraslado.RETIRO)).isPresent();
        assertThat(trasladoRepository.findByEstadiaIdAndTipo(400L, TipoTraslado.DEVOLUCION)).isEmpty();
    }

    @Test
    void guardaRutaConParadas() {
        Traslado t = new Traslado();
        t.setEstadiaId(300L);
        t.setClienteId(3L);
        t.setDireccionId(30L);
        t.setTipo(TipoTraslado.RETIRO);
        t.setOrigenLatitud(-31.4201);
        t.setOrigenLongitud(-64.1888);
        t.setDestinoLatitud(-31.4);
        t.setDestinoLongitud(-64.2);
        t.setFechaProgramada(LocalDate.now().plusDays(3));
        t = trasladoRepository.saveAndFlush(t);

        RutaDiaria ruta = new RutaDiaria();
        ruta.setFecha(LocalDate.now().plusDays(3));
        ruta.setTipo(TipoTraslado.RETIRO);
        ruta.setDistanciaTotalKm(12.5);
        ruta.setDuracionTotalMin(25);
        ruta.setEstimada(false);
        ruta = rutaDiariaRepository.saveAndFlush(ruta);

        ParadaRuta parada = new ParadaRuta();
        parada.setRuta(ruta);
        parada.setTraslado(t);
        parada.setOrden(1);
        parada.setDistanciaDesdeAnteriorKm(12.5);
        parada.setDuracionDesdeAnteriorMin(25);
        paradaRutaRepository.saveAndFlush(parada);

        List<ParadaRuta> paradas = paradaRutaRepository.findByRutaIdOrderByOrdenAsc(ruta.getId());
        assertThat(paradas).hasSize(1);
        assertThat(paradaRutaRepository.findByTrasladoId(t.getId())).isPresent();
    }
}
