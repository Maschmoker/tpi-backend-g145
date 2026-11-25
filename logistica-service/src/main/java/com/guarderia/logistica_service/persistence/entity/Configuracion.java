package com.guarderia.logistica_service.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "configuracion")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "guarderia_latitud", nullable = false)
    private Double guarderiaLatitud;

    @Column(name = "guarderia_longitud", nullable = false)
    private Double guarderiaLongitud;

    @Column(name = "guarderia_direccion", nullable = false, length = 255)
    private String guarderiaDireccion;

    @Column(name = "radio_cobertura_km", nullable = false)
    private Double radioCoberturaKm;

    @Column(name = "tarifa_por_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaPorKm;

    @Column(name = "costo_minimo_traslado", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoMinimoTraslado;

    @Column(name = "capacidad_diaria_traslados", nullable = false)
    private Integer capacidadDiariaTraslados;

    @Column(name = "radio_agrupacion_km", nullable = false)
    private Double radioAgrupacionKm;

    @Column(name = "velocidad_promedio_kmh", nullable = false)
    private Double velocidadPromedioKmh;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
