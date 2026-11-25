package com.guarderia.estadias_service.dto.mapper;

import com.guarderia.estadias_service.dto.response.ServicioCatalogoResponse;
import com.guarderia.estadias_service.persistence.entity.ServicioCatalogo;
import com.guarderia.estadias_service.service.CatalogoService;

import java.util.List;

public final class ServicioCatalogoMapper {
    private ServicioCatalogoMapper() {
    }

    public static ServicioCatalogoResponse toResponse(ServicioCatalogo s) {
        return new ServicioCatalogoResponse(s.getId(), s.getNombre(), s.getDescripcion(), s.getTipoServicio(),
                s.getPrecioPequeno(), s.getPrecioMediano(), s.getPrecioGrande(),
                CatalogoService.especiesAplicables(s), s.isActivo());
    }

    public static ServicioCatalogo toEntity(com.guarderia.estadias_service.dto.request.ServicioCatalogoRequest r) {
        ServicioCatalogo s = new ServicioCatalogo();
        s.setNombre(r.nombre());
        s.setDescripcion(r.descripcion());
        s.setTipoServicio(r.tipoServicio());
        s.setPrecioPequeno(r.precioPequeno());
        s.setPrecioMediano(r.precioMediano());
        s.setPrecioGrande(r.precioGrande());
        s.setEspeciesAplicables(r.especiesAplicables());
        s.setActivo(r.activo());
        return s;
    }
}