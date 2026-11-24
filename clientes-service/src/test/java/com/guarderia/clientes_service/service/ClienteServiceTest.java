package com.guarderia.clientes_service.service;

import com.guarderia.clientes_service.dto.ClientePerfilRequest;
import com.guarderia.clientes_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.clientes_service.persistence.entity.Cliente;
import com.guarderia.clientes_service.persistence.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Authentication authCliente(String subject) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject(subject).build();
        return new UsernamePasswordAuthenticationToken(jwt, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }

    @Test
    void upsertCreaPerfilSiNoExiste() {
        Authentication auth = authCliente("kc-1");
        when(clienteRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        var resp = clienteService.crearOActualizarPerfilActual(auth,
                new ClientePerfilRequest("Ana", "Perez", "ana@x.com", "123"));

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.keycloakId()).isEqualTo("kc-1");
        assertThat(resp.nombre()).isEqualTo("Ana");
    }

    @Test
    void upsertActualizaPerfilExistente() {
        Authentication auth = authCliente("kc-1");
        Cliente existente = new Cliente();
        existente.setId(5L);
        existente.setKeycloakId("kc-1");
        when(clienteRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = clienteService.crearOActualizarPerfilActual(auth,
                new ClientePerfilRequest("Ana2", "Perez2", "ana2@x.com", "999"));

        assertThat(resp.id()).isEqualTo(5L);
        assertThat(resp.nombre()).isEqualTo("Ana2");
    }

    @Test
    void clienteNoPuedeConsultarPerfilDeOtro() {
        Authentication auth = authCliente("kc-1");
        Cliente otro = new Cliente();
        otro.setId(7L);
        otro.setKeycloakId("kc-otro");
        when(clienteRepository.findById(7L)).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> clienteService.obtenerPorId(auth, 7L))
                .isInstanceOf(AccesoDenegadoRecursoException.class);
    }
}