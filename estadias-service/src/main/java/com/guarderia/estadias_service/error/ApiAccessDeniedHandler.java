package com.guarderia.estadias_service.error;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import com.guarderia.commons.error.ApiError;
import com.guarderia.commons.error.ApiErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public ApiAccessDeniedHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String correlationId = CorrelationIdUtils.actual();
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtils.normalizar(request.getHeader(CorrelationId.HEADER));
        }

        ApiError error = ApiErrorFactory.crear(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tiene permisos para realizar esta operación", request.getRequestURI(), correlationId);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(CorrelationId.HEADER, correlationId);
        jsonMapper.writeValue(response.getOutputStream(), error);
    }
}