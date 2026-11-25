package com.guarderia.logistica_service.service;

import com.guarderia.commons.geo.Coordenada;
import com.guarderia.logistica_service.domain.EstadoTraslado;
import com.guarderia.logistica_service.domain.TipoTraslado;
import com.guarderia.logistica_service.exception.RecursoNoEncontradoException;
import com.guarderia.logistica_service.exception.ReglaNegocioException;
import com.guarderia.logistica_service.persistence.entity.Configuracion;
import com.guarderia.logistica_service.persistence.entity.Traslado;
import com.guarderia.logistica_service.persistence.repository.TrasladoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class TrasladoService {

    private final TrasladoRepository trasladoRepository;
    private final ConfiguracionService configuracionService;
    private final DistanciaService distanciaService;

    public TrasladoService(TrasladoRepository trasladoRepository, ConfiguracionService configuracionService,
                           DistanciaService distanciaService) {
        this.trasladoRepository = trasladoRepository;
        this.configuracionService = configuracionService;
        this.distanciaService = distanciaService;
    }

    public record CotizacionRequest(Long estadiaId, Long direccionId, Double origenLat, Double origenLng,
                                    Double destinoLat, Double destinoLng, LocalDate fecha, boolean idaYVuelta) {}

    public record CotizacionResponse(BigDecimal costoTraslado, Double distanciaKm, Integer duracionMin, boolean estimado) {}

    public record CrearRequest(Long estadiaId, Long clienteId, String tipo, Long direccionId, Double latitud,
                               Double longitud, LocalDate fechaProgramada, String direccionTexto) {}

    public record TrasladoResponse(Long id, String estado) {}

    @Transactional(readOnly = true)
    public CotizacionResponse cotizar(CotizacionRequest req) {
        if (req.origenLat() == null || req.origenLng() == null) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "COORDENADAS_OBLIGATORIAS", "Las coordenadas de origen son obligatorias");
        }
        Configuracion cfg = configuracionService.obtener();
        Coordenada origen = new Coordenada(req.origenLat(), req.origenLng());
        Coordenada destino;
        if (req.destinoLat() != null && req.destinoLng() != null) {
            destino = new Coordenada(req.destinoLat(), req.destinoLng());
        } else {
            destino = new Coordenada(cfg.getGuarderiaLatitud(), cfg.getGuarderiaLongitud());
        }
        var res = distanciaService.entre(origen, destino);
        double distanciaBase = res.distanciaKm();
        int duracionBase = res.duracionMin();
        double distanciaTotal = req.idaYVuelta() ? distanciaBase * 2.0 : distanciaBase;
        int duracionTotal = req.idaYVuelta() ? duracionBase * 2 : duracionBase;

        // cobertura sobre distancia base (one-way)
        if (distanciaBase > cfg.getRadioCoberturaKm()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "FUERA_COBERTURA",
                    "La dirección está fuera del radio de cobertura (" + cfg.getRadioCoberturaKm() + " km)");
        }

        BigDecimal costo = cfg.getTarifaPorKm().multiply(BigDecimal.valueOf(distanciaTotal));
        if (costo.compareTo(cfg.getCostoMinimoTraslado()) < 0) {
            costo = cfg.getCostoMinimoTraslado();
        }
        return new CotizacionResponse(costo, distanciaTotal, duracionTotal, res.estimado());
    }

    @Transactional
    public TrasladoResponse crear(CrearRequest req) {
        if (req.latitud() == null || req.longitud() == null || req.estadiaId() == null || req.clienteId() == null) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "DATOS_OBLIGATORIOS", "Faltan datos obligatorios del traslado");
        }
        TipoTraslado tipo;
        try {
            tipo = TipoTraslado.valueOf(req.tipo());
        } catch (Exception e) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "TIPO_INVALIDO", "Tipo de traslado inválido");
        }

        // idempotencia
        var existente = trasladoRepository.findByEstadiaIdAndTipo(req.estadiaId(), tipo);
        if (existente.isPresent()) {
            return new TrasladoResponse(existente.get().getId(), existente.get().getEstado().name());
        }

        Configuracion cfg = configuracionService.obtener();
        Coordenada cliente = new Coordenada(req.latitud(), req.longitud());
        Coordenada guarderia = new Coordenada(cfg.getGuarderiaLatitud(), cfg.getGuarderiaLongitud());

        // calcular distancia con estimado
        var res = distanciaService.entre(
                tipo == TipoTraslado.RETIRO ? guarderia : cliente,
                tipo == TipoTraslado.RETIRO ? cliente : guarderia);

        if (res.distanciaKm() > cfg.getRadioCoberturaKm()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "FUERA_COBERTURA",
                    "La dirección está fuera del radio de cobertura");
        }

        // capacidad diaria
        long activos = trasladoRepository.countByFechaProgramadaAndEstadoNotIn(req.fechaProgramada(),
                Set.of(EstadoTraslado.CANCELADO));
        if (activos >= cfg.getCapacidadDiariaTraslados()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "CAPACIDAD_SUPERADA",
                    "Se superó la capacidad diaria de traslados");
        }

        BigDecimal costo = cfg.getTarifaPorKm().multiply(BigDecimal.valueOf(res.distanciaKm()));
        if (costo.compareTo(cfg.getCostoMinimoTraslado()) < 0) {
            costo = cfg.getCostoMinimoTraslado();
        }

        Traslado t = new Traslado();
        t.setEstadiaId(req.estadiaId());
        t.setClienteId(req.clienteId());
        t.setDireccionId(req.direccionId());
        t.setTipo(tipo);
        t.setDireccionTexto(req.direccionTexto());
        if (tipo == TipoTraslado.RETIRO) {
            t.setOrigenLatitud(guarderia.latitud());
            t.setOrigenLongitud(guarderia.longitud());
            t.setDestinoLatitud(cliente.latitud());
            t.setDestinoLongitud(cliente.longitud());
            t.setEstado(EstadoTraslado.PROGRAMADO);
        } else {
            t.setOrigenLatitud(cliente.latitud());
            t.setOrigenLongitud(cliente.longitud());
            t.setDestinoLatitud(guarderia.latitud());
            t.setDestinoLongitud(guarderia.longitud());
            t.setEstado(EstadoTraslado.PENDIENTE);
        }
        t.setFechaProgramada(req.fechaProgramada());
        t.setDistanciaKm(res.distanciaKm());
        t.setDuracionEstimadaMin(res.duracionMin());
        t.setCostoCalculado(costo);
        t.setEstimado(res.estimado());

        Traslado guardado = trasladoRepository.save(t);
        return new TrasladoResponse(guardado.getId(), guardado.getEstado().name());
    }

    @Transactional
    public void habilitarPorId(Long id) {
        Traslado t = trasladoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Traslado no encontrado"));
        if (t.getEstado() == EstadoTraslado.PENDIENTE) {
            t.setEstado(EstadoTraslado.PROGRAMADO);
            trasladoRepository.save(t);
        }
    }

    @Transactional
    public void habilitarPorEstadia(Long estadiaId) {
        List<Traslado> traslados = trasladoRepository.findByEstadiaId(estadiaId);
        for (Traslado t : traslados) {
            if (t.getTipo() == TipoTraslado.DEVOLUCION && t.getEstado() == EstadoTraslado.PENDIENTE) {
                t.setEstado(EstadoTraslado.PROGRAMADO);
                trasladoRepository.save(t);
            }
        }
    }

    @Transactional
    public void cancelarPorEstadia(Long estadiaId) {
        List<Traslado> traslados = trasladoRepository.findByEstadiaId(estadiaId);
        for (Traslado t : traslados) {
            if (t.getEstado() != EstadoTraslado.COMPLETADO && t.getEstado() != EstadoTraslado.CANCELADO) {
                t.setEstado(EstadoTraslado.CANCELADO);
                trasladoRepository.save(t);
            }
        }
    }

    @Transactional(readOnly = true)
    public Traslado obtener(Long id) {
        return trasladoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Traslado no encontrado"));
    }

    @Transactional
    public void cambiarEstado(Long id, EstadoTraslado nuevo) {
        Traslado t = obtener(id);
        t.setEstado(nuevo);
        trasladoRepository.save(t);
    }

    public List<Traslado> elegibles(LocalDate fecha, TipoTraslado tipo) {
        return trasladoRepository.findByFechaProgramadaAndTipoAndEstadoIn(fecha, tipo,
                List.of(EstadoTraslado.PROGRAMADO, EstadoTraslado.PENDIENTE));
    }
}