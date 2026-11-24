package com.guarderia.commons.correlacion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdUtilsTest {

    @AfterEach
    void limpiarMdc() {
        CorrelationIdUtils.limpiar();
    }

    @Test
    void generaIdentificadoresValidosYDistintos() {
        String primero = CorrelationIdUtils.generar();
        String segundo = CorrelationIdUtils.generar();

        assertThat(primero).matches(CorrelationId.FORMATO_VALIDO.pattern());
        assertThat(segundo).matches(CorrelationId.FORMATO_VALIDO.pattern()).isNotEqualTo(primero);
    }

    @Test
    void conservaUnValorValidoYRecortaEspacios() {
        assertThat(CorrelationIdUtils.normalizar("  abc-123_ABC.def:1  "))
                .isEqualTo("abc-123_ABC.def:1");
    }

    @Test
    void generaUnoNuevoSiElValorEsNuloOVacio() {
        assertThat(CorrelationIdUtils.normalizar(null)).matches(CorrelationId.FORMATO_VALIDO.pattern());
        assertThat(CorrelationIdUtils.normalizar("   ")).matches(CorrelationId.FORMATO_VALIDO.pattern());
    }

    @Test
    void descartaValoresConCaracteresInvalidos() {
        assertThat(CorrelationIdUtils.normalizar("id\ninyectado"))
                .matches(CorrelationId.FORMATO_VALIDO.pattern())
                .doesNotContain("\n");
    }

    @Test
    void descartaValoresDemasiadoLargos() {
        String demasiadoLargo = "a".repeat(CorrelationId.LONGITUD_MAXIMA + 1);

        assertThat(CorrelationIdUtils.normalizar(demasiadoLargo))
                .matches(CorrelationId.FORMATO_VALIDO.pattern())
                .isNotEqualTo(demasiadoLargo);
    }

    @Test
    void publicaYlimpiaElIdentificadorEnElMdc() {
        CorrelationIdUtils.establecer("corr-123");

        assertThat(CorrelationIdUtils.actual()).isEqualTo("corr-123");

        CorrelationIdUtils.limpiar();

        assertThat(CorrelationIdUtils.actual()).isNull();
    }
}
