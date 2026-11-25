package com.guarderia.gateway_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingSecurityTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void sinToken_clientesMe_devuelve401JsonConCorrelationId() {
        webTestClient.get().uri("/api/clientes/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().exists("X-Correlation-ID")
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.correlationId").isNotEmpty();
    }

    @Test
    void health_sinToken_devuelve200Up() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void propagacionCorrelationId_yEnrutamientoBasico() {
        String cid = "test-correlation-123";
        webTestClient.get().uri("/api/clientes/me")
                .header("X-Correlation-ID", cid)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().valueEquals("X-Correlation-ID", cid)
                .expectBody()
                .jsonPath("$.correlationId").isEqualTo(cid);

        // Enrutamiento básico: la ruta existe (401 por seguridad, no 404)
        webTestClient.get().uri("/api/estadias/inexistente")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
