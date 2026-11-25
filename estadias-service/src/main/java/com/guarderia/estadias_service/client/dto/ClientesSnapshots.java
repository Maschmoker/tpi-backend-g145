package com.guarderia.estadias_service.client.dto;

import java.math.BigDecimal;

public final class ClientesSnapshots {
    private ClientesSnapshots() {
    }

    public record ClienteSnapshot(Long id, String keycloakId, String nombre, String apellido, boolean activo) {
    }

    public record MascotaSnapshot(Long id, Long clienteId, String nombre, String especie, String tamanio,
                                 BigDecimal pesoKg, boolean activa) {
    }

    public record DireccionSnapshot(Long id, Long clienteId, String calle, String numero, String ciudad,
                                   Double latitud, Double longitud) {
    }
}