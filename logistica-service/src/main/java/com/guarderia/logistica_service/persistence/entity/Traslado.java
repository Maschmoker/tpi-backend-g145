package com.guarderia.logistica_service.persistence.entity;

import com.guarderia.logistica_service.domain.EstadoTraslado;
import com.guarderia.logistica_service.domain.TipoTraslado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "traslados")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Traslado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "estadia_id", nullable = false)
    private Long estadiaId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "direccion_id", nullable = false)
    private Long direccionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTraslado tipo;

    @Column(name = "direccion_texto", length = 500)
    private String direccionTexto;

    @Column(name = "origen_latitud", nullable = false)
    private Double origenLatitud;

    @Column(name = "origen_longitud", nullable = false)
    private Double origenLongitud;

    @Column(name = "destino_latitud", nullable = false)
    private Double destinoLatitud;

    @Column(name = "destino_longitud", nullable = false)
    private Double destinoLongitud;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTraslado estado = EstadoTraslado.PENDIENTE;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "duracion_estimada_min")
    private Integer duracionEstimadaMin;

    @Column(name = "costo_calculado", precision = 10, scale = 2)
    private BigDecimal costoCalculado;

    @Column(nullable = false)
    private boolean estimado = false;

    @Column(name = "chofer_keycloak_id", length = 64)
    private String choferKeycloakId;

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
        if (this.estado == null) this.estado = EstadoTraslado.PENDIENTE;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
