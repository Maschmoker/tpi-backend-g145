package com.guarderia.estadias_service.controller;

import com.guarderia.estadias_service.service.CatalogoService;
import com.guarderia.estadias_service.service.EstadiaCuidadoService;
import com.guarderia.estadias_service.service.EstadiaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.guarderia.estadias_service.config.SecurityConfig;
import com.guarderia.estadias_service.error.ApiAccessDeniedHandler;
import com.guarderia.estadias_service.error.ApiAuthenticationEntryPoint;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CatalogoController.class, EstadiaController.class, EstadiaServicioController.class})
@Import({SecurityConfig.class, ApiAuthenticationEntryPoint.class, ApiAccessDeniedHandler.class})
class EstadiasWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoService catalogoService;
    @MockitoBean
    private EstadiaService estadiaService;
    @MockitoBean
    private EstadiaCuidadoService estadiaCuidadoService;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/estadias")).andExpect(status().isUnauthorized());
    }

    @Test
    void clienteListaEstadias() throws Exception {
        org.mockito.Mockito.when(estadiaService.listar(any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/estadias")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                                .jwt(j -> j.subject("kc-1"))))
                .andExpect(status().isOk());
    }

    @Test
    void validacionCotizarDevuelve400() throws Exception {
        mockMvc.perform(post("/api/estadias/cotizar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                                .jwt(j -> j.subject("kc-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void personalCuidadoNoPuedeCrearEstadia() throws Exception {
        mockMvc.perform(post("/api/estadias")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PERSONAL_CUIDADO"))
                                .jwt(j -> j.subject("kc-2")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mascotaId\":1,\"modalidad\":\"PRESENCIAL\",\"requiereRetiro\":false,\"requiereDevolucion\":false,\"fechaIngresoPlanificada\":\"2026-12-01\",\"fechaEgresoPlanificada\":\"2026-12-02\",\"serviciosCatalogoIds\":[1]}"))
                .andExpect(status().isForbidden());
    }
}