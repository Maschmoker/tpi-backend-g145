package com.guarderia.logistica_service.persistence.entity;

import com.guarderia.logistica_service.domain.EstadoRuta;
import com.guarderia.logistica_service.domain.TipoTraslado;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutas_diarias")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RutaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTraslado tipo;

    @Column(name = "chofer_keycloak_id", length = 64)
    private String choferKeycloakId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;

    @Column(name = "distancia_total_km")
    private Double distanciaTotalKm;

    @Column(name = "duracion_total_min")
    private Integer duracionTotalMin;

    @Column(nullable = false)
    private boolean estimada = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ParadaRuta> paradas = new ArrayList<>();

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
        if (this.estado == null) this.estado = EstadoRuta.PLANIFICADA;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
