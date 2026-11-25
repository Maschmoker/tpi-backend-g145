package com.guarderia.estadias_service.service;

import com.guarderia.estadias_service.domain.TipoServicio;
import com.guarderia.estadias_service.exception.RecursoNoEncontradoException;
import com.guarderia.estadias_service.persistence.entity.ServicioCatalogo;
import com.guarderia.estadias_service.persistence.repository.ServicioCatalogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoService {

    private final ServicioCatalogoRepository repository;

    public CatalogoService(ServicioCatalogoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ServicioCatalogo> listarActivos(String especie, TipoServicio tipo) {
        List<ServicioCatalogo> todos = repository.findByActivoTrue();
        return todos.stream()
                .filter(s -> especie == null || especiesAplicables(s).contains(especie.trim().toUpperCase()))
                .filter(s -> tipo == null || s.getTipoServicio() == tipo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioCatalogo obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio de catálogo no encontrado: " + id));
    }

    @Transactional
    public ServicioCatalogo crear(ServicioCatalogo servicio) {
        return repository.save(servicio);
    }

    @Transactional
    public ServicioCatalogo actualizar(Long id, ServicioCatalogo datos) {
        ServicioCatalogo existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setTipoServicio(datos.getTipoServicio());
        existente.setPrecioPequeno(datos.getPrecioPequeno());
        existente.setPrecioMediano(datos.getPrecioMediano());
        existente.setPrecioGrande(datos.getPrecioGrande());
        existente.setEspeciesAplicables(datos.getEspeciesAplicables());
        existente.setActivo(datos.isActivo());
        return repository.save(existente);
    }

    @Transactional
    public ServicioCatalogo cambiarActivo(Long id, boolean activo) {
        ServicioCatalogo existente = obtenerPorId(id);
        existente.setActivo(activo);
        return repository.save(existente);
    }

    public static List<String> especiesAplicables(ServicioCatalogo servicio) {
        return java.util.Arrays.stream(servicio.getEspeciesAplicables().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .toList();
    }
}