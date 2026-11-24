package com.guarderia.commons.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class HaversineTest {

    private static final Coordenada CORDOBA = new Coordenada(-31.4201, -64.1888);
    private static final Coordenada BUENOS_AIRES = new Coordenada(-34.6037, -58.3816);

    @Test
    void laDistanciaEntreUnPuntoYSiMismoEsCero() {
        assertThat(Haversine.distanciaKm(CORDOBA, CORDOBA)).isCloseTo(0.0, within(1e-9));
    }

    @Test
    void unGradoDeLatitudEquivaleAproximadamenteA111Kilometros() {
        Coordenada unGradoAlSur = new Coordenada(-32.0, -64.0);

        assertThat(Haversine.distanciaKm(new Coordenada(-31.0, -64.0), unGradoAlSur))
                .isCloseTo(111.1951, within(0.05));
    }

    @Test
    void calculaLaDistanciaEntreCordobaYBuenosAires() {
        assertThat(Haversine.distanciaKm(CORDOBA, BUENOS_AIRES)).isCloseTo(646.742, within(0.5));
    }

    @Test
    void laDistanciaEsSimetrica() {
        assertThat(Haversine.distanciaKm(CORDOBA, BUENOS_AIRES))
                .isCloseTo(Haversine.distanciaKm(BUENOS_AIRES, CORDOBA), within(1e-9));
    }

    @Test
    void laDistanciaEnMetrosEsMilVecesLaDistanciaEnKilometros() {
        assertThat(Haversine.distanciaMetros(CORDOBA, BUENOS_AIRES))
                .isCloseTo(Haversine.distanciaKm(CORDOBA, BUENOS_AIRES) * 1000.0, within(1e-6));
    }

    @Test
    void rechazaCoordenadasNulas() {
        assertThatThrownBy(() -> Haversine.distanciaKm(null, CORDOBA))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Haversine.distanciaKm(CORDOBA, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void soportaElCruceDelMeridianoDe180Grados() {
        Coordenada este = new Coordenada(0.0, 179.9);
        Coordenada oeste = new Coordenada(0.0, -179.9);

        assertThat(Haversine.distanciaKm(este, oeste)).isCloseTo(22.24, within(0.05));
    }
}
