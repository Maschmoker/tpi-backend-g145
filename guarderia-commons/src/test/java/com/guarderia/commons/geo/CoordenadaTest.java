package com.guarderia.commons.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordenadaTest {

    @Test
    void aceptaLosLimitesDelRango() {
        Coordenada coordenada = new Coordenada(90.0, 180.0);

        assertThat(coordenada.latitud()).isEqualTo(90.0);
        assertThat(coordenada.longitud()).isEqualTo(180.0);
        assertThat(new Coordenada(-90.0, -180.0)).isEqualTo(new Coordenada(-90.0, -180.0));
    }

    @Test
    void rechazaLatitudFueraDeRango() {
        assertThatThrownBy(() -> new Coordenada(90.0001, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitud");

        assertThatThrownBy(() -> new Coordenada(-90.0001, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitud");
    }

    @Test
    void rechazaLongitudFueraDeRango() {
        assertThatThrownBy(() -> new Coordenada(0.0, 180.0001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitud");

        assertThatThrownBy(() -> new Coordenada(0.0, -180.0001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitud");
    }

    @Test
    void rechazaValoresNoFinitos() {
        assertThatThrownBy(() -> new Coordenada(Double.NaN, 0.0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Coordenada(0.0, Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
