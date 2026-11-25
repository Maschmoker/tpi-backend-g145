package com.guarderia.logistica_service.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paradas_ruta")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParadaRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private RutaDiaria ruta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "traslado_id", nullable = false, unique = true)
    private Traslado traslado;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "distancia_desde_anterior_km")
    private Double distanciaDesdeAnteriorKm;

    @Column(name = "duracion_desde_anterior_min")
    private Integer duracionDesdeAnteriorMin;
}
