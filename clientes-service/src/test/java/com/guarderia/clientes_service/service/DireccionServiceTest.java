package com.guarderia.clientes_service.service;

import com.guarderia.clientes_service.dto.DireccionRequest;
import com.guarderia.clientes_service.persistence.entity.Cliente;
import com.guarderia.clientes_service.persistence.entity.Direccion;
import com.guarderia.clientes_service.persistence.repository.ClienteRepository;
import com.guarderia.clientes_service.persistence.repository.DireccionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DireccionServiceTest {

    @Mock
    private DireccionRepository direccionRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private DireccionService direccionService;

    private Authentication authCliente(String subject) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject(subject).build();
        return new UsernamePasswordAuthenticationToken(jwt, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }

    @Test
    void crearDireccionPrincipalDesmarcaLaAnterior() {
        Authentication auth = authCliente("kc-1");
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setKeycloakId("kc-1");
        when(clienteRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(cliente));

        Direccion existentePrincipal = new Direccion();
        existentePrincipal.setId(5L);
        existentePrincipal.setCliente(cliente);
        existentePrincipal.setEsPrincipal(true);
        when(direccionRepository.findByClienteIdAndEsPrincipalTrue(1L)).thenReturn(Optional.of(existentePrincipal));

        when(direccionRepository.save(any(Direccion.class))).thenAnswer(inv -> {
            Direccion d = inv.getArgument(0);
            if (d.getId() == null) d.setId(9L);
            return d;
        });

        var resp = direccionService.crearPropia(auth, new DireccionRequest(
                "Av. Colón", "123", null, null, null, null, -31.4, -64.1, true));

        assertThat(resp.esPrincipal()).isTrue();
        verify(direccionRepository).save(existentePrincipal);
        assertThat(existentePrincipal.isEsPrincipal()).isFalse();
    }

    @Test
    void defaultCiudadCordobaSiVacia() {
        Authentication auth = authCliente("kc-1");
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setKeycloakId("kc-1");
        when(clienteRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(cliente));
        when(direccionRepository.save(any(Direccion.class))).thenAnswer(inv -> {
            Direccion d = inv.getArgument(0);
            d.setId(3L);
            return d;
        });

        var resp = direccionService.crearPropia(auth, new DireccionRequest(
                "Calle", "1", null, null, null, "  ", null, null, false));

        assertThat(resp.ciudad()).isEqualTo("Córdoba");
    }
}