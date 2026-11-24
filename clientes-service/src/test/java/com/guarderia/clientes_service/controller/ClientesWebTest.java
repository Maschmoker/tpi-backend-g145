package com.guarderia.clientes_service.controller;

import com.guarderia.clientes_service.dto.ClienteResponse;
import com.guarderia.clientes_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.clientes_service.service.ClienteService;
import com.guarderia.clientes_service.service.DireccionService;
import com.guarderia.clientes_service.service.MascotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.guarderia.clientes_service.config.SecurityConfig;
import com.guarderia.clientes_service.error.ApiAccessDeniedHandler;
import com.guarderia.clientes_service.error.ApiAuthenticationEntryPoint;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ClienteController.class, DireccionController.class, MascotaController.class})
@Import({SecurityConfig.class, ApiAuthenticationEntryPoint.class, ApiAccessDeniedHandler.class})
class ClientesWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;
    @MockitoBean
    private DireccionService direccionService;
    @MockitoBean
    private MascotaService mascotaService;

    @Test
    void sinTokenDevuelve401Json() throws Exception {
        mockMvc.perform(get("/api/clientes/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteObtieneSuPerfil() throws Exception {
        when(clienteService.obtenerPerfilActual(any())).thenReturn(
                new ClienteResponse(1L, "kc-1", "Ana", "Perez", "a@x.com", "1", true,
                        LocalDateTime.now(), LocalDateTime.now()));
        mockMvc.perform(get("/api/clientes/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                                .jwt(j -> j.subject("kc-1"))))
                .andExpect(status().isOk());
    }

    @Test
    void adminListaClientes() throws Exception {
        when(clienteService.listarTodos()).thenReturn(List.of());
        mockMvc.perform(get("/api/clientes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoPuedeListarTodos() throws Exception {
        mockMvc.perform(get("/api/clientes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteNoPuedeConsultarOtroPerfil() throws Exception {
        when(clienteService.obtenerPorId(any(), eq(99L)))
                .thenThrow(new AccesoDenegadoRecursoException("no"));
        mockMvc.perform(get("/api/clientes/99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                                .jwt(j -> j.subject("kc-1"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void validacionMascotaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/clientes/me/mascotas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                                .jwt(j -> j.subject("kc-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}