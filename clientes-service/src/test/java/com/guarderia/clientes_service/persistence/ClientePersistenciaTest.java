package com.guarderia.clientes_service.persistence;

import com.guarderia.clientes_service.domain.Especie;
import com.guarderia.clientes_service.domain.TamanioMascota;
import com.guarderia.clientes_service.persistence.entity.Cliente;
import com.guarderia.clientes_service.persistence.entity.Direccion;
import com.guarderia.clientes_service.persistence.entity.Mascota;
import com.guarderia.clientes_service.persistence.repository.ClienteRepository;
import com.guarderia.clientes_service.persistence.repository.DireccionRepository;
import com.guarderia.clientes_service.persistence.repository.MascotaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class ClientePersistenciaTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Test
    void guardaYRecuperaClienteConDireccionesYMascotas() {
        Cliente cliente = new Cliente();
        cliente.setKeycloakId("kc-123");
        cliente.setNombre("Ana");
        cliente.setApellido("Perez");
        cliente.setEmail("ana@example.com");
        cliente = clienteRepository.saveAndFlush(cliente);

        Direccion direccion = new Direccion();
        direccion.setCliente(cliente);
        direccion.setCalle("Av. Colón");
        direccion.setNumero("123");
        direccion.setEsPrincipal(true);
        direccionRepository.saveAndFlush(direccion);

        Mascota mascota = new Mascota();
        mascota.setCliente(cliente);
        mascota.setNombre("Firulais");
        mascota.setEspecie(Especie.PERRO);
        mascota.setTamanio(TamanioMascota.MEDIANO);
        mascota.setPesoKg(new BigDecimal("12.50"));
        mascota.setActiva(true);
        mascotaRepository.saveAndFlush(mascota);

        assertThat(clienteRepository.findByKeycloakId("kc-123")).isPresent();
        assertThat(direccionRepository.findByCliente_KeycloakId("kc-123")).hasSize(1);
        assertThat(mascotaRepository.findByCliente_KeycloakIdAndActivaTrue("kc-123")).hasSize(1);
        assertThat(mascotaRepository.countByClienteIdAndActivaTrue(cliente.getId())).isEqualTo(1);
    }

    @Test
    void validaPropiedadPorKeycloakId() {
        Cliente cliente = new Cliente();
        cliente.setKeycloakId("kc-owner");
        cliente.setNombre("Luis");
        cliente.setApellido("Gomez");
        cliente = clienteRepository.saveAndFlush(cliente);

        Mascota mascota = new Mascota();
        mascota.setCliente(cliente);
        mascota.setNombre("Mishi");
        mascota.setEspecie(Especie.GATO);
        mascota.setTamanio(TamanioMascota.PEQUENO);
        mascota.setActiva(true);
        mascota = mascotaRepository.saveAndFlush(mascota);

        assertThat(mascotaRepository.findByIdAndCliente_KeycloakId(mascota.getId(), "kc-owner")).isPresent();
        assertThat(mascotaRepository.findByIdAndCliente_KeycloakId(mascota.getId(), "kc-otro")).isEmpty();
        assertThat(mascotaRepository.existsByIdAndCliente_KeycloakIdAndActivaTrue(mascota.getId(), "kc-otro")).isFalse();
        assertThat(direccionRepository.findByIdAndCliente_KeycloakId(999999L, "kc-owner")).isEmpty();
    }
}