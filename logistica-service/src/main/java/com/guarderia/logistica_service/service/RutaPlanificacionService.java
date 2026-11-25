package com.guarderia.logistica_service.service;

import com.guarderia.commons.geo.Coordenada;
import com.guarderia.logistica_service.domain.EstadoRuta;
import com.guarderia.logistica_service.domain.EstadoTraslado;
import com.guarderia.logistica_service.domain.TipoTraslado;
import com.guarderia.logistica_service.exception.RecursoNoEncontradoException;
import com.guarderia.logistica_service.exception.ReglaNegocioException;
import com.guarderia.logistica_service.persistence.entity.Configuracion;
import com.guarderia.logistica_service.persistence.entity.ParadaRuta;
import com.guarderia.logistica_service.persistence.entity.Configuracion;
import com.guarderia.logistica_service.persistence.entity.RutaDiaria;
import com.guarderia.logistica_service.persistence.entity.Traslado;
import com.guarderia.logistica_service.persistence.repository.ParadaRutaRepository;
import com.guarderia.logistica_service.persistence.repository.RutaDiariaRepository;
import com.guarderia.logistica_service.client.EstadiasHttpClient;
import com.guarderia.logistica_service.persistence.repository.TrasladoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RutaPlanificacionService {

    private static final Logger log = LoggerFactory.getLogger(RutaPlanificacionService.class);

    private final TrasladoService trasladoService;
    private final ConfiguracionService configuracionService;
    private final DistanciaService distanciaService;
    private final RutaDiariaRepository rutaRepository;
    private final ParadaRutaRepository paradaRepository;
    private final TrasladoRepository trasladoRepository;
    private final EstadiasHttpClient estadiasHttpClient;

    public RutaPlanificacionService(TrasladoService trasladoService, ConfiguracionService configuracionService,
                                    DistanciaService distanciaService, RutaDiariaRepository rutaRepository,
                                    ParadaRutaRepository paradaRepository, TrasladoRepository trasladoRepository,
                                    EstadiasHttpClient estadiasHttpClient) {
        this.trasladoService = trasladoService;
        this.configuracionService = configuracionService;
        this.distanciaService = distanciaService;
        this.rutaRepository = rutaRepository;
        this.paradaRepository = paradaRepository;
        this.trasladoRepository = trasladoRepository;
        this.estadiasHttpClient = estadiasHttpClient;
    }

    public record GenerarRequest(LocalDate fecha, String tipo) {}

    @Transactional
    public RutaDiaria generar(GenerarRequest req, String token) {
        TipoTraslado tipo;
        try {
            tipo = TipoTraslado.valueOf(req.tipo());
        } catch (Exception e) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "TIPO_INVALIDO", "Tipo de traslado inválido");
        }

        List<Traslado> elegibles = trasladoService.elegibles(req.fecha(), tipo);
        if (elegibles.isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "SIN_TRASLADOS", "No hay traslados elegibles para la fecha y tipo");
        }

        Configuracion cfg = configuracionService.obtener();
        Coordenada guarderia = new Coordenada(cfg.getGuarderiaLatitud(), cfg.getGuarderiaLongitud());

        // Vecino más cercano
        List<Traslado> ordenados = new ArrayList<>();
        List<Traslado> restantes = new ArrayList<>(elegibles);
        Coordenada actual = guarderia;
        while (!restantes.isEmpty()) {
            Traslado mejor = null;
            double mejorDist = Double.MAX_VALUE;
            for (Traslado t : restantes) {
                Coordenada c = new Coordenada(t.getDestinoLatitud(), t.getDestinoLongitud());
                // for retiro, destino es cliente; for devolucion, origen es cliente. Use the stop coordinate.
                Coordenada parada = stopCoord(t);
                double d = distanciaService.entre(actual, parada).distanciaKm();
                if (d < mejorDist) {
                    mejorDist = d;
                    mejor = t;
                }
            }
            ordenados.add(mejor);
            restantes.remove(mejor);
            actual = stopCoord(mejor);
        }

        // 2-opt
        ordenados = twoOpt(ordenados, guarderia);

        // Persistir
        RutaDiaria ruta = new RutaDiaria();
        ruta.setFecha(req.fecha());
        ruta.setTipo(tipo);
        ruta.setEstado(EstadoRuta.PLANIFICADA);

        List<ParadaRuta> paradas = new ArrayList<>();
        double totalKm = 0.0;
        int totalMin = 0;
        boolean estimada = false;
        Coordenada prev = guarderia;
        int orden = 1;
        for (Traslado t : ordenados) {
            Coordenada parada = stopCoord(t);
            var res = distanciaService.entre(prev, parada);
            ParadaRuta p = new ParadaRuta();
            p.setTraslado(t);
            p.setOrden(orden++);
            p.setDistanciaDesdeAnteriorKm(res.distanciaKm());
            p.setDuracionDesdeAnteriorMin(res.duracionMin());
            paradas.add(p);
            totalKm += res.distanciaKm();
            totalMin += res.duracionMin();
            if (res.estimado()) estimada = true;
            prev = parada;
        }
        // regreso a guardería
        var regreso = distanciaService.entre(prev, guarderia);
        totalKm += regreso.distanciaKm();
        totalMin += regreso.duracionMin();
        if (regreso.estimado()) estimada = true;

        ruta.setDistanciaTotalKm(totalKm);
        ruta.setDuracionTotalMin(totalMin);
        ruta.setEstimada(estimada);

        RutaDiaria guardada = rutaRepository.save(ruta);
        for (ParadaRuta p : paradas) {
            p.setRuta(guardada);
            paradaRepository.save(p);
        }
        guardada.setParadas(paradas);
        return guardada;
    }

    private Coordenada stopCoord(Traslado t) {
        // The stop is the client location: for RETIRO, destino (client); for DEVOLUCION, origen (client)
        if (t.getTipo() == TipoTraslado.RETIRO) {
            return new Coordenada(t.getDestinoLatitud(), t.getDestinoLongitud());
        }
        return new Coordenada(t.getOrigenLatitud(), t.getOrigenLongitud());
    }

    private List<Traslado> twoOpt(List<Traslado> paradas, Coordenada guarderia) {
        List<Traslado> mejor = new ArrayList<>(paradas);
        boolean improved = true;
        int iter = 0;
        while (improved && iter < 1000) {
            improved = false;
            iter++;
            for (int i = 0; i < mejor.size() - 1; i++) {
                for (int j = i + 1; j < mejor.size(); j++) {
                    List<Traslado> candidato = aplicarSwap(mejor, i, j);
                    if (costoTotal(candidato, guarderia) < costoTotal(mejor, guarderia) - 1e-9) {
                        mejor = candidato;
                        improved = true;
                    }
                }
            }
        }
        return mejor;
    }

    private List<Traslado> aplicarSwap(List<Traslado> lista, int i, int j) {
        List<Traslado> res = new ArrayList<>(lista);
        // reverse segment [i..j]
        int a = i, b = j;
        while (a < b) {
            Traslado tmp = res.get(a);
            res.set(a, res.get(b));
            res.set(b, tmp);
            a++;
            b--;
        }
        return res;
    }

    private double costoTotal(List<Traslado> paradas, Coordenada guarderia) {
        double total = 0.0;
        Coordenada prev = guarderia;
        for (Traslado t : paradas) {
            Coordenada c = stopCoord(t);
            total += distanciaService.entre(prev, c).distanciaKm();
            prev = c;
        }
        total += distanciaService.entre(prev, guarderia).distanciaKm();
        return total;
    }

    @Transactional
    public RutaDiaria iniciar(Long rutaId, String token, String choferKeycloakId) {
        RutaDiaria ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada"));
        if (ruta.getEstado() != EstadoRuta.PLANIFICADA) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA", "La ruta no puede iniciarse");
        }
        ruta.setEstado(EstadoRuta.EN_CURSO);
        ruta.setChoferKeycloakId(choferKeycloakId);
        rutaRepository.save(ruta);
        TipoTraslado tipo = ruta.getTipo();
        for (ParadaRuta p : ruta.getParadas()) {
            Traslado t = p.getTraslado();
            if (t.getEstado() == EstadoTraslado.PROGRAMADO || t.getEstado() == EstadoTraslado.PENDIENTE) {
                t.setEstado(EstadoTraslado.EN_CURSO);
                trasladoRepository.save(t);
                try {
                    if (tipo == TipoTraslado.RETIRO) estadiasHttpClient.marcarTransitoIda(token, t.getEstadiaId());
                    else estadiasHttpClient.marcarTransitoVuelta(token, t.getEstadiaId());
                } catch (Exception ignored) {}
            }
        }
        return ruta;
    }

    @Transactional
    public RutaDiaria completarParada(Long rutaId, int orden, String token) {
        RutaDiaria ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada"));
        ParadaRuta parada = ruta.getParadas().stream()
                .filter(p -> p.getOrden() == orden).findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Parada no encontrada"));
        Traslado t = parada.getTraslado();
        if (t.getEstado() == EstadoTraslado.EN_CURSO) {
            t.setEstado(EstadoTraslado.COMPLETADO);
            trasladoRepository.save(t);
            try {
                if (ruta.getTipo() == TipoTraslado.RETIRO) estadiasHttpClient.marcarRecibida(token, t.getEstadiaId());
                else estadiasHttpClient.marcarEntregada(token, t.getEstadiaId());
            } catch (Exception ignored) {}
        }
        // si todas completadas -> finalizar ruta
        boolean todas = ruta.getParadas().stream().allMatch(p -> {
            Traslado tr = p.getTraslado();
            return tr.getEstado() == EstadoTraslado.COMPLETADO;
        });
        if (todas) {
            ruta.setEstado(EstadoRuta.FINALIZADA);
            rutaRepository.save(ruta);
        }
        return ruta;
    }

    @Transactional(readOnly = true)
    public RutaDiaria obtener(Long id) {
        RutaDiaria r = rutaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada"));
        // ensure paradas loaded
        r.getParadas().size();
        return r;
    }

    @Transactional(readOnly = true)
    public List<RutaDiaria> listar(LocalDate fecha, String tipo) {
        List<RutaDiaria> lista;
        if (fecha != null && tipo != null) {
            TipoTraslado t = TipoTraslado.valueOf(tipo);
            lista = rutaRepository.findAll().stream()
                    .filter(r -> r.getFecha().equals(fecha) && r.getTipo() == t).toList();
        } else if (fecha != null) {
            lista = rutaRepository.findAll().stream().filter(r -> r.getFecha().equals(fecha)).toList();
        } else {
            lista = rutaRepository.findAll();
        }
        lista.forEach(r -> r.getParadas().size());
        return lista;
    }
}