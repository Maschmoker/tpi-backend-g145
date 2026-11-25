package com.guarderia.estadias_service.service;

import com.guarderia.commons.security.RolesGuarderia;
import com.guarderia.estadias_service.client.ClientesHttpClient;
import com.guarderia.estadias_service.client.LogisticaHttpClient;
import com.guarderia.estadias_service.client.dto.ClientesSnapshots;
import com.guarderia.estadias_service.client.dto.LogisticaDtos;
import com.guarderia.estadias_service.domain.EstadoEstadia;
import com.guarderia.estadias_service.domain.ModalidadEstadia;
import com.guarderia.estadias_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.estadias_service.exception.RecursoNoEncontradoException;
import com.guarderia.estadias_service.exception.ReglaNegocioException;
import com.guarderia.estadias_service.persistence.entity.Estadia;
import com.guarderia.estadias_service.persistence.entity.EstadiaServicio;
import com.guarderia.estadias_service.persistence.entity.HistorialEstado;
import com.guarderia.estadias_service.persistence.entity.ServicioCatalogo;
import com.guarderia.estadias_service.persistence.repository.EstadiaRepository;
import com.guarderia.estadias_service.persistence.repository.EstadiaServicioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class EstadiaService {

    public static final Set<EstadoEstadia> ESTADOS_ACTIVOS = EnumSet.of(
            EstadoEstadia.SOLICITADA, EstadoEstadia.RETIRO_PROGRAMADO, EstadoEstadia.EN_TRANSITO_IDA,
            EstadoEstadia.RECIBIDA, EstadoEstadia.EN_CUIDADO, EstadoEstadia.LISTA_PARA_RETIRO,
            EstadoEstadia.EN_TRANSITO_VUELTA);

    public static final Set<EstadoEstadia> ESTADOS_CANCELABLES = EnumSet.of(
            EstadoEstadia.SOLICITADA, EstadoEstadia.RETIRO_PROGRAMADO);

    private final EstadiaRepository estadiaRepository;
    private final EstadiaServicioRepository estadiaServicioRepository;
    private final ClientesHttpClient clientesHttpClient;
    private final LogisticaHttpClient logisticaHttpClient;
    private final EstadiaEstadoService estadoService;
    private final CatalogoService catalogoService;

    public EstadiaService(EstadiaRepository estadiaRepository,
                          EstadiaServicioRepository estadiaServicioRepository,
                          ClientesHttpClient clientesHttpClient,
                          LogisticaHttpClient logisticaHttpClient,
                          EstadiaEstadoService estadoService,
                          CatalogoService catalogoService) {
        this.estadiaRepository = estadiaRepository;
        this.estadiaServicioRepository = estadiaServicioRepository;
        this.clientesHttpClient = clientesHttpClient;
        this.logisticaHttpClient = logisticaHttpClient;
        this.estadoService = estadoService;
        this.catalogoService = catalogoService;
    }

    @Transactional(readOnly = true)
    public List<Estadia> listar(Authentication auth) {
        if (esCliente(auth)) {
            return estadiaRepository.findByClienteKeycloakId(keycloakId(auth));
        }
        return estadiaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Estadia obtener(Long id, Authentication auth) {
        Estadia estadia = estadiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("EstadÃ­a no encontrada: " + id));
        verificarPropiedad(estadia, auth);
        return estadia;
    }

    @Transactional
    public Estadia cotizarYCrear(CrearEstadiaInput input, Authentication auth, boolean soloCotizar) {
        String token = token(auth);
        ClientesSnapshots.ClienteSnapshot cliente = clientesHttpClient.obtenerClienteActual(token);
        if (!cliente.activo()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "CLIENTE_INACTIVO", "El cliente no estÃ¡ activo");
        }
        ClientesSnapshots.MascotaSnapshot mascota = clientesHttpClient.obtenerMascotaPropia(token, input.mascotaId());
        if (!mascota.activa()) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "MASCOTA_INACTIVA", "La mascota no estÃ¡ activa");
        }
        if (!mascota.clienteId().equals(cliente.id())) {
            throw new AccesoDenegadoRecursoException("La mascota no pertenece al cliente autenticado");
        }
        if (estadiaRepository.existsByMascotaIdAndEstadoIn(mascota.id(), ESTADOS_ACTIVOS)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "MASCOTA_CON_ESTADIA_ACTIVA",
                    "La mascota ya tiene una estadÃ­a activa");
        }
        if (input.fechaEgreso().isBefore(input.fechaIngreso())) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "FECHAS_INVALIDAS",
                    "La fecha de egreso no puede ser anterior a la fecha de ingreso");
        }
        if (input.serviciosIds() == null || input.serviciosIds().isEmpty()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "SIN_SERVICIOS",
                    "Toda estadÃ­a debe incluir al menos un servicio");
        }

        List<ServicioCatalogo> servicios = input.serviciosIds().stream()
                .map(id -> {
                    ServicioCatalogo sc = catalogoPorId(id);
                    if (!CatalogoService.especiesAplicables(sc).contains(mascota.especie().toUpperCase())) {
                        throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "SERVICIO_NO_APLICABLE",
                                "El servicio " + sc.getNombre() + " no aplica a la especie " + mascota.especie());
                    }
                    return sc;
                })
                .toList();

        boolean requiereTraslado = input.modalidad() == ModalidadEstadia.CON_TRASLADO
                || input.requiereRetiro() || input.requiereDevolucion();
        ClientesSnapshots.DireccionSnapshot direccion = null;
        if (requiereTraslado) {
            if (input.direccionTrasladoId() == null) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "DIRECCION_REQUERIDA",
                        "El traslado requiere una direcciÃ³n");
            }
            direccion = clientesHttpClient.obtenerDireccionPropia(token, input.direccionTrasladoId());
            if (direccion.latitud() == null || direccion.longitud() == null) {
                throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "COORDENADAS_REQUERIDAS",
                        "La direcciÃ³n de traslado debe tener coordenadas");
            }
            if (!direccion.clienteId().equals(cliente.id())) {
                throw new AccesoDenegadoRecursoException("La direcciÃ³n no pertenece al cliente autenticado");
            }
        }

        int dias = (int) (input.fechaEgreso().toEpochDay() - input.fechaIngreso().toEpochDay() + 1);
        if (dias < 1) {
            dias = 1;
        }

        BigDecimal costoServicios = BigDecimal.ZERO;
        for (ServicioCatalogo sc : servicios) {
            BigDecimal precio = precioPorTamanio(sc, mascota.tamanio());
            costoServicios = costoServicios.add(precio.multiply(BigDecimal.valueOf(dias)));
        }

        BigDecimal costoTraslado = BigDecimal.ZERO;
        if (requiereTraslado && direccion != null) {
            LogisticaDtos.CotizacionTrasladoResponse cot = logisticaHttpClient.cotizarTraslado(token,
                    new LogisticaDtos.CotizacionTrasladoRequest(null, direccion.id(), direccion.latitud(), direccion.longitud(),
                            null, null, input.fechaIngreso(), true));
            costoTraslado = cot.costoTraslado() != null ? cot.costoTraslado() : BigDecimal.ZERO;
        }
        BigDecimal costoTotal = costoServicios.add(costoTraslado);

        if (soloCotizar) {
            throw new CotizacionResultadoException(costoServicios, costoTraslado, costoTotal, dias, servicios);
        }

        EstadoEstadia estadoInicial = input.requiereRetiro() ? EstadoEstadia.RETIRO_PROGRAMADO : EstadoEstadia.SOLICITADA;

        Estadia estadia = new Estadia();
        estadia.setClienteId(cliente.id());
        estadia.setClienteKeycloakId(cliente.keycloakId());
        estadia.setMascotaId(mascota.id());
        estadia.setMascotaNombreSnapshot(mascota.nombre());
        estadia.setMascotaEspecieSnapshot(mascota.especie());
        estadia.setMascotaTamanioSnapshot(mascota.tamanio());
        estadia.setModalidad(input.modalidad());
        estadia.setRequiereRetiro(input.requiereRetiro());
        estadia.setRequiereDevolucion(input.requiereDevolucion());
        estadia.setDireccionTrasladoId(requiereTraslado ? input.direccionTrasladoId() : null);
        estadia.setEstado(estadoInicial);
        estadia.setFechaIngresoPlanificada(input.fechaIngreso());
        estadia.setFechaEgresoPlanificada(input.fechaEgreso());
        estadia.setCostoServicios(costoServicios);
        estadia.setCostoTraslado(costoTraslado);
        estadia.setCostoTotal(costoTotal);
        estadia.setObservaciones(input.observaciones());

        List<EstadiaServicio> items = new ArrayList<>();
        for (ServicioCatalogo sc : servicios) {
            EstadiaServicio item = new EstadiaServicio();
            item.setEstadia(estadia);
            item.setServicioCatalogo(sc);
            item.setPrecioAplicado(precioPorTamanio(sc, mascota.tamanio()));
            items.add(item);
        }
        estadia.setServicios(items);

        HistorialEstado historial = new HistorialEstado();
        historial.setEstadia(estadia);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo(estadoInicial);
        historial.setUsuarioKeycloakId(cliente.keycloakId());
        historial.setObservacion("CreaciÃ³n de estadÃ­a");
        estadia.getHistorial().add(historial);

        Estadia guardada = estadiaRepository.save(estadia);

        // IntegraciÃ³n con logÃ­stica: crear traslados
        if (input.requiereRetiro()) {
            logisticaHttpClient.crearTraslado(token, new LogisticaDtos.CrearTrasladoRequest(
                    guardada.getId(), cliente.id(), "RETIRO", input.direccionTrasladoId(),
                    direccion.latitud(), direccion.longitud(), input.fechaIngreso()));
        }
        if (input.requiereDevolucion()) {
            LogisticaDtos.TrasladoResponse trasladoDev = logisticaHttpClient.crearTraslado(token, new LogisticaDtos.CrearTrasladoRequest(
                    guardada.getId(), cliente.id(), "DEVOLUCION", input.direccionTrasladoId(),
                    direccion.latitud(), direccion.longitud(), input.fechaEgreso()));
            // se mantiene pendiente; se habilita al pasar a LISTA_PARA_RETIRO
            guardada.setObservaciones((guardada.getObservaciones() == null ? "" : guardada.getObservaciones())
                    + " [trasladoDevolucionId=" + trasladoDev.id() + "]");
            estadiaRepository.save(guardada);
        }

        return guardada;
    }

    // Wrapper para exponer cotizaciÃ³n sin excepciÃ³n interna
    public CotizacionResultado cotizar(CrearEstadiaInput input, Authentication auth) {
        try {
            cotizarYCrear(input, auth, true);
        } catch (CotizacionResultadoException e) {
            return new CotizacionResultado(e.costoServicios, e.costoTraslado, e.costoTotal, e.dias, e.servicios);
        }
        throw new IllegalStateException("No se pudo obtener cotizaciÃ³n");
    }

    @Transactional
    public Estadia crear(CrearEstadiaInput input, Authentication auth) {
        return cotizarYCrear(input, auth, false);
    }

    @Transactional
    public void cancelar(Long id, Authentication auth) {
        Estadia estadia = obtener(id, auth);
        if (!ESTADOS_CANCELABLES.contains(estadia.getEstado())) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "CANCELACION_NO_PERMITIDA",
                    "La estadÃ­a solo puede cancelarse antes de la recepciÃ³n");
        }
        EstadoEstadia anterior = estadia.getEstado();
        estadoService.validarTransicion(anterior, EstadoEstadia.CANCELADA);
        estadia.setEstado(EstadoEstadia.CANCELADA);
        registrarHistorial(estadia, anterior, EstadoEstadia.CANCELADA, keycloakId(auth), "CancelaciÃ³n de estadÃ­a");
        estadiaRepository.save(estadia);
        // compensaciÃ³n
        try {
            logisticaHttpClient.cancelarTrasladosPorEstadia(token(auth), estadia.getId());
        } catch (Exception ex) {
            // best-effort
        }
    }

    @Transactional
    public Estadia recibir(Long id, Authentication auth) {
        verificarRol(auth, RolesGuarderia.RECEPCIONISTA, RolesGuarderia.ADMIN);
        Estadia estadia = obtenerInterno(id);
        EstadoEstadia anterior = estadia.getEstado();
        if (anterior != EstadoEstadia.SOLICITADA && anterior != EstadoEstadia.EN_TRANSITO_IDA) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA",
                    "La estadÃ­a no puede recibirse desde el estado " + anterior);
        }
        estadoService.validarTransicion(anterior, EstadoEstadia.RECIBIDA);
        estadia.setEstado(EstadoEstadia.RECIBIDA);
        estadia.setFechaRecepcionReal(LocalDateTime.now());
        registrarHistorial(estadia, anterior, EstadoEstadia.RECIBIDA, keycloakId(auth), "RecepciÃ³n de mascota");
        return estadiaRepository.save(estadia);
    }

    @Transactional
    public Estadia iniciarCuidado(Long id, Authentication auth) {
        verificarRol(auth, RolesGuarderia.RECEPCIONISTA, RolesGuarderia.ADMIN, RolesGuarderia.PERSONAL_CUIDADO);
        Estadia estadia = obtenerInterno(id);
        EstadoEstadia anterior = estadia.getEstado();
        estadoService.validarTransicion(anterior, EstadoEstadia.EN_CUIDADO);
        estadia.setEstado(EstadoEstadia.EN_CUIDADO);
        registrarHistorial(estadia, anterior, EstadoEstadia.EN_CUIDADO, keycloakId(auth), "Inicio de cuidado");
        return estadiaRepository.save(estadia);
    }

    @Transactional
    public Estadia entregar(Long id, Authentication auth) {
        verificarRol(auth, RolesGuarderia.RECEPCIONISTA, RolesGuarderia.ADMIN);
        Estadia estadia = obtenerInterno(id);
        EstadoEstadia anterior = estadia.getEstado();
        if (anterior != EstadoEstadia.LISTA_PARA_RETIRO && anterior != EstadoEstadia.EN_TRANSITO_VUELTA) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA",
                    "La estadÃ­a no estÃ¡ lista para entrega");
        }
        estadoService.validarTransicion(anterior, EstadoEstadia.ENTREGADA);
        estadia.setEstado(EstadoEstadia.ENTREGADA);
        estadia.setFechaEntregaReal(LocalDateTime.now());
        registrarHistorial(estadia, anterior, EstadoEstadia.ENTREGADA, keycloakId(auth), "Entrega de mascota");
        return estadiaRepository.save(estadia);
    }

    @Transactional
    public Estadia transitoIda(Long id, Authentication auth) {
        verificarRol(auth, RolesGuarderia.CHOFER, RolesGuarderia.ADMIN);
        Estadia estadia = obtenerInterno(id);
        EstadoEstadia anterior = estadia.getEstado();
        estadoService.validarTransicion(anterior, EstadoEstadia.EN_TRANSITO_IDA);
        estadia.setEstado(EstadoEstadia.EN_TRANSITO_IDA);
        registrarHistorial(estadia, anterior, EstadoEstadia.EN_TRANSITO_IDA, keycloakId(auth), "En trÃ¡nsito (ida)");
        return estadiaRepository.save(estadia);
    }

    @Transactional
    public Estadia marcarRecibidaPorChofer(Long id, Authentication auth) {
        verificarRol(auth, RolesGuarderia.CHOFER, RolesGuarderia.ADMIN);
        Estadia estadia = obtenerInterno(id);
        EstadoEstadia anterior = estadia.getEstado();
        estadoService.validarTransicion(anterior, EstadoEstadia.RECIBIDA);
        estadia.setEstado(EstadoEstadia.RECIBIDA);
        estadia.setFechaRecepcionReal(LocalDateTime.now());
        registrarHistorial(estadia, anterior, EstadoEstadia.RECIBIDA, keycloakId(auth), "RecepciÃ³n por chofer");
        return estadiaRepository.save(estadia);
    }

    @Transactional
    public Estadia transitoVuelta(Long id, Authentication auth) {
        verificarRol(auth, RolesGuarderia.CHOFER, RolesGuarderia.ADMIN);
        Estadia estadia = obtenerInterno(id);
        EstadoEstadia anterior = estadia.getEstado();
        estadoService.validarTransicion(anterior, EstadoEstadia.EN_TRANSITO_VUELTA);
        estadia.setEstado(EstadoEstadia.EN_TRANSITO_VUELTA);
        registrarHistorial(estadia, anterior, EstadoEstadia.EN_TRANSITO_VUELTA, keycloakId(auth), "En trÃ¡nsito (vuelta)");
        // habilitar traslado de devoluciÃ³n si existe
        if (estadia.isRequiereDevolucion() && estadia.getDireccionTrasladoId() != null) {
            String token = token(auth);
            // se asume que el traslado se habilita por estadÃ­a; logÃ­stica resuelve el id
            logisticaHttpClient.habilitarTrasladoPorEstadia(token, estadia.getId());
        }
        return estadiaRepository.save(estadia);
    }

    @Transactional
    public Estadia marcarEntregadaPorChofer(Long id, Authentication auth) {
        verificarRol(auth, RolesGuarderia.CHOFER, RolesGuarderia.ADMIN);
        Estadia estadia = obtenerInterno(id);
        EstadoEstadia anterior = estadia.getEstado();
        estadoService.validarTransicion(anterior, EstadoEstadia.ENTREGADA);
        estadia.setEstado(EstadoEstadia.ENTREGADA);
        estadia.setFechaEntregaReal(LocalDateTime.now());
        registrarHistorial(estadia, anterior, EstadoEstadia.ENTREGADA, keycloakId(auth), "Entrega por chofer");
        return estadiaRepository.save(estadia);
    }

    @Transactional
    public void verificarTransicionAutomaticaLEstadoLista(Long estadiaId, Authentication auth) {
        Estadia estadia = obtenerInterno(estadiaId);
        if (estadia.getEstado() == EstadoEstadia.EN_CUIDADO) {
            long pendientes = estadiaServicioRepository.countByEstadiaIdAndEstadoNot(estadiaId,
                    com.guarderia.estadias_service.domain.EstadoServicioCuidado.REALIZADO);
            if (pendientes == 0 && !estadiaServicioRepository.findByEstadiaId(estadiaId).isEmpty()) {
                EstadoEstadia anterior = estadia.getEstado();
                estadoService.validarTransicion(anterior, EstadoEstadia.LISTA_PARA_RETIRO);
                estadia.setEstado(EstadoEstadia.LISTA_PARA_RETIRO);
                registrarHistorial(estadia, anterior, EstadoEstadia.LISTA_PARA_RETIRO, keycloakId(auth),
                        "Todos los servicios realizados");
                estadiaRepository.save(estadia);
                if (estadia.isRequiereDevolucion() && estadia.getDireccionTrasladoId() != null) {
                    try {
                        logisticaHttpClient.habilitarTrasladoPorEstadia(token(auth), estadiaId);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    // --- helpers ---

    private Estadia obtenerInterno(Long id) {
        return estadiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("EstadÃ­a no encontrada: " + id));
    }

    private void verificarPropiedad(Estadia estadia, Authentication auth) {
        if (esCliente(auth) && !estadia.getClienteKeycloakId().equals(keycloakId(auth))) {
            throw new AccesoDenegadoRecursoException("No tiene permisos sobre esta estadÃ­a");
        }
    }

    private void verificarRol(Authentication auth, String... roles) {
        for (String rol : roles) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol))) {
                return;
            }
        }
        throw new AccesoDenegadoRecursoException("No tiene permisos para realizar esta operaciÃ³n");
    }

    private boolean esCliente(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    }

    private String keycloakId(Authentication auth) {
        return ((Jwt) auth.getPrincipal()).getSubject();
    }

    private String token(Authentication auth) {
        return ((Jwt) auth.getPrincipal()).getTokenValue();
    }

    private ServicioCatalogo catalogoPorId(Long id) {
        return catalogoService.obtenerPorId(id);
    }

    private void registrarHistorial(Estadia estadia, EstadoEstadia anterior, EstadoEstadia nuevo, String usuario, String obs) {
        HistorialEstado h = new HistorialEstado();
        h.setEstadia(estadia);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setUsuarioKeycloakId(usuario);
        h.setObservacion(obs);
        estadia.getHistorial().add(h);
    }

    private BigDecimal precioPorTamanio(ServicioCatalogo sc, String tamanio) {
        return switch (tamanio.toUpperCase()) {
            case "PEQUENO", "PEQUEÃ‘O", "PEQUENA" -> sc.getPrecioPequeno();
            case "MEDIANO" -> sc.getPrecioMediano();
            case "GRANDE" -> sc.getPrecioGrande();
            default -> sc.getPrecioPequeno();
        };
    }

    // Input DTO interno
    public record CrearEstadiaInput(
            Long mascotaId,
            ModalidadEstadia modalidad,
            boolean requiereRetiro,
            boolean requiereDevolucion,
            Long direccionTrasladoId,
            LocalDate fechaIngreso,
            LocalDate fechaEgreso,
            List<Long> serviciosIds,
            String observaciones
    ) {
    }

    // Resultado de cotizaciÃ³n
    public record CotizacionResultado(
            BigDecimal costoServicios,
            BigDecimal costoTraslado,
            BigDecimal costoTotal,
            int dias,
            List<ServicioCatalogo> servicios
    ) {
    }

    // ExcepciÃ³n interna para cotizaciÃ³n
    public static class CotizacionResultadoException extends RuntimeException {
        public final BigDecimal costoServicios;
        public final BigDecimal costoTraslado;
        public final BigDecimal costoTotal;
        public final int dias;
        public final List<ServicioCatalogo> servicios;

        public CotizacionResultadoException(BigDecimal costoServicios, BigDecimal costoTraslado, BigDecimal costoTotal,
                                           int dias, List<ServicioCatalogo> servicios) {
            this.costoServicios = costoServicios;
            this.costoTraslado = costoTraslado;
            this.costoTotal = costoTotal;
            this.dias = dias;
            this.servicios = servicios;
        }
    }
}
