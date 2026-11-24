package com.guarderia.clientes_service.service;

import com.guarderia.clientes_service.domain.Especie;
import com.guarderia.clientes_service.domain.TamanioMascota;
import com.guarderia.clientes_service.dto.MascotaRequest;
import com.guarderia.clientes_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.clientes_service.persistence.entity.Cliente;
import com.guarderia.clientes_service.persistence.entity.Mascota;
import com.guarderia.clientes_service.persistence.repository.ClienteRepository;
import com.guarderia.clientes_service.persistence.repository.MascotaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private MascotaService mascotaService;

    private Authentication authCliente(String subject) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject(subject).build();
        return new UsernamePasswordAuthenticationToken(jwt, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }

    @Test
    void bajaLogicaMarcaInactiva() {
        Authentication auth = authCliente("kc-1");
        Mascota m = new Mascota();
        m.setId(4L);
        Cliente c = new Cliente();
        c.setId(1L);
        c.setKeycloakId("kc-1");
        m.setCliente(c);
        m.setActiva(true);
        m.setNombre("Firulais");
        m.setEspecie(Especie.PERRO);
        m.setTamanio(TamanioMascota.MEDIANO);
        when(mascotaRepository.findByIdAndCliente_KeycloakId(4L, "kc-1")).thenReturn(Optional.of(m));
        when(mascotaRepository.save(any(Mascota.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = mascotaService.bajaLogica(auth, 4L);

        assertThat(resp.activa()).isFalse();
    }

    @Test
    void clienteNoPuedeConsultarMascotaDeOtro() {
        Authentication auth = authCliente("kc-1");
        Cliente propio = new Cliente();
        propio.setId(1L);
        propio.setKeycloakId("kc-1");
        when(clienteRepository.findByKeycloakId("kc-1")).thenReturn(Optional.of(propio));

        assertThatThrownBy(() -> mascotaService.listarPorCliente(auth, 99L, true))
                .isInstanceOf(AccesoDenegadoRecursoException.class);
    }

    @Test
    void registrarMascotaExigePerfil() {
        Authentication auth = authCliente("kc-1");
        when(clienteRepository.findByKeycloakId("kc-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mascotaService.registrarPropia(auth, new MascotaRequest(
                "Mishi", Especie.GATO, null, TamanioMascota.PEQUENO, new BigDecimal("3.0"), null)))
                .isInstanceOf(com.guarderia.clientes_service.exception.ReglaNegocioException.class);
    }
}