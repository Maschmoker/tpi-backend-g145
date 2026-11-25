package com.guarderia.estadias_service.dto.mapper;

import com.guarderia.estadias_service.domain.EstadoServicioCuidado;
import com.guarderia.estadias_service.dto.response.EstadiaResponse;
import com.guarderia.estadias_service.dto.response.EstadiaServicioResponse;
import com.guarderia.estadias_service.persistence.entity.Estadia;
import com.guarderia.estadias_service.persistence.entity.EstadiaServicio;

import java.util.List;

public final class EstadiaMapper {
    private EstadiaMapper() {
    }

    public static EstadiaResponse toResponse(Estadia e) {
        List<EstadiaServicioResponse> servicios = e.getServicios().stream()
                .map(EstadiaMapper::toServicioResponse)
                .toList();
        return new EstadiaResponse(
                e.getId(), e.getClienteId(), e.getClienteKeycloakId(), e.getMascotaId(),
                e.getMascotaNombreSnapshot(), e.getMascotaEspecieSnapshot(), e.getMascotaTamanioSnapshot(),
                e.getModalidad(), e.isRequiereRetiro(), e.isRequiereDevolucion(), e.getDireccionTrasladoId(),
                e.getEstado(), e.getFechaIngresoPlanificada(), e.getFechaEgresoPlanificada(),
                e.getFechaRecepcionReal(), e.getFechaEntregaReal(), e.getCostoServicios(), e.getCostoTraslado(),
                e.getCostoTotal(), e.getObservaciones(), servicios);
    }

    public static EstadiaServicioResponse toServicioResponse(EstadiaServicio s) {
        String nombre = s.getServicioCatalogo() != null ? s.getServicioCatalogo().getNombre() : null;
        return new EstadiaServicioResponse(s.getId(), s.getServicioCatalogo() != null ? s.getServicioCatalogo().getId() : null,
                nombre, s.getPrecioAplicado(), s.getEstado(), s.getResponsableKeycloakId(), s.getFechaInicio(),
                s.getFechaFinalizacion());
    }
}