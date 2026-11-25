package com.guarderia.estadias_service.service;

import com.guarderia.commons.security.RolesGuarderia;
import com.guarderia.estadias_service.domain.EstadoServicioCuidado;
import com.guarderia.estadias_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.estadias_service.exception.RecursoNoEncontradoException;
import com.guarderia.estadias_service.exception.ReglaNegocioException;
import com.guarderia.estadias_service.persistence.entity.Estadia;
import com.guarderia.estadias_service.persistence.entity.EstadiaServicio;
import com.guarderia.estadias_service.persistence.repository.EstadiaServicioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EstadiaCuidadoService {

    private final EstadiaServicioRepository repository;
    private final EstadiaService estadiaService;

    public EstadiaCuidadoService(EstadiaServicioRepository repository, EstadiaService estadiaService) {
        this.repository = repository;
        this.estadiaService = estadiaService;
    }

    @Transactional(readOnly = true)
    public List<EstadiaServicio> listar(Long estadiaId, Authentication auth) {
        Estadia estadia = estadiaService.obtener(estadiaId, auth);
        return repository.findByEstadiaId(estadia.getId());
    }

    @Transactional
    public EstadiaServicio iniciar(Long estadiaId, Long itemId, Authentication auth) {
        verificarRol(auth, RolesGuarderia.PERSONAL_CUIDADO, RolesGuarderia.RECEPCIONISTA, RolesGuarderia.ADMIN);
        Estadia estadia = estadiaService.obtener(estadiaId, auth);
        if (estadia.getEstado() != com.guarderia.estadias_service.domain.EstadoEstadia.EN_CUIDADO) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "ESTADIA_NO_EN_CUIDADO",
                    "La estadía debe estar EN_CUIDADO para iniciar servicios");
        }
        EstadiaServicio item = repository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio de estadía no encontrado: " + itemId));
        if (!item.getEstadia().getId().equals(estadiaId)) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "ITEM_NO_PERTENECE", "El servicio no pertenece a la estadía");
        }
        if (item.getEstado() != EstadoServicioCuidado.PENDIENTE) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA",
                    "Solo se puede iniciar un servicio en estado PENDIENTE");
        }
        item.setEstado(EstadoServicioCuidado.EN_CURSO);
        item.setResponsableKeycloakId(keycloakId(auth));
        item.setFechaInicio(LocalDateTime.now());
        return repository.save(item);
    }

    @Transactional
    public EstadiaServicio realizar(Long estadiaId, Long itemId, Authentication auth) {
        verificarRol(auth, RolesGuarderia.PERSONAL_CUIDADO, RolesGuarderia.RECEPCIONISTA, RolesGuarderia.ADMIN);
        Estadia estadia = estadiaService.obtener(estadiaId, auth);
        EstadiaServicio item = repository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio de estadía no encontrado: " + itemId));
        if (!item.getEstadia().getId().equals(estadiaId)) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "ITEM_NO_PERTENECE", "El servicio no pertenece a la estadía");
        }
        if (item.getEstado() != EstadoServicioCuidado.EN_CURSO) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA",
                    "Solo se puede finalizar un servicio en estado EN_CURSO");
        }
        item.setEstado(EstadoServicioCuidado.REALIZADO);
        item.setFechaFinalizacion(LocalDateTime.now());
        EstadiaServicio guardado = repository.save(item);
        estadiaService.verificarTransicionAutomaticaLEstadoLista(estadiaId, auth);
        return guardado;
    }

    private void verificarRol(Authentication auth, String... roles) {
        for (String rol : roles) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol))) {
                return;
            }
        }
        throw new AccesoDenegadoRecursoException("No tiene permisos para realizar esta operación");
    }

    private String keycloakId(Authentication auth) {
        return ((Jwt) auth.getPrincipal()).getSubject();
    }
}