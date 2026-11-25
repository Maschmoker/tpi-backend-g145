package com.guarderia.estadias_service.error;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import com.guarderia.commons.error.ApiError;
import com.guarderia.commons.error.ApiErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public ApiAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        String correlationId = CorrelationIdUtils.actual();
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtils.normalizar(request.getHeader(CorrelationId.HEADER));
        }

        ApiError error = ApiErrorFactory.crear(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                "Se requiere autenticación", request.getRequestURI(), correlationId);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(CorrelationId.HEADER, correlationId);
        jsonMapper.writeValue(response.getOutputStream(), error);
    }
}