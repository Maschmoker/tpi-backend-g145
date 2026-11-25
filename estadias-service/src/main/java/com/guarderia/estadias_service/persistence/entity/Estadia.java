package com.guarderia.estadias_service.persistence.entity;

import com.guarderia.estadias_service.domain.EstadoEstadia;
import com.guarderia.estadias_service.domain.ModalidadEstadia;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estadias")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Estadia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "cliente_keycloak_id", nullable = false, length = 64)
    private String clienteKeycloakId;

    @Column(name = "mascota_id", nullable = false)
    private Long mascotaId;

    @Column(name = "mascota_nombre_snapshot", nullable = false, length = 100)
    private String mascotaNombreSnapshot;

    @Column(name = "mascota_especie_snapshot", nullable = false, length = 30)
    private String mascotaEspecieSnapshot;

    @Column(name = "mascota_tamanio_snapshot", nullable = false, length = 20)
    private String mascotaTamanioSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModalidadEstadia modalidad;

    @Column(name = "requiere_retiro", nullable = false)
    private boolean requiereRetiro = false;

    @Column(name = "requiere_devolucion", nullable = false)
    private boolean requiereDevolucion = false;

    @Column(name = "direccion_traslado_id")
    private Long direccionTrasladoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEstadia estado = EstadoEstadia.SOLICITADA;

    @Column(name = "fecha_ingreso_planificada", nullable = false)
    private LocalDate fechaIngresoPlanificada;

    @Column(name = "fecha_egreso_planificada", nullable = false)
    private LocalDate fechaEgresoPlanificada;

    @Column(name = "fecha_recepcion_real")
    private LocalDateTime fechaRecepcionReal;

    @Column(name = "fecha_entrega_real")
    private LocalDateTime fechaEntregaReal;

    @Column(name = "costo_servicios", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoServicios = BigDecimal.ZERO;

    @Column(name = "costo_traslado", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTraslado = BigDecimal.ZERO;

    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "estadia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EstadiaServicio> servicios = new ArrayList<>();

    @OneToMany(mappedBy = "estadia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistorialEstado> historial = new ArrayList<>();

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
