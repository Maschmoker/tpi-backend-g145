package com.guarderia.commons.correlacion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro servlet que garantiza la propagación del {@code X-Correlation-ID}.
 *
 * <p>Normaliza el encabezado entrante (o genera uno nuevo), lo publica como atributo del
 * request, en el MDC de SLF4J y en la respuesta, y limpia el MDC al finalizar. No se registra
 * automáticamente: cada servicio debe declararlo como bean:</p>
 *
 * <pre>{@code
 * @Bean
 * FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
 *     var registro = new FilterRegistrationBean<>(new CorrelationIdFilter());
 *     registro.addUrlPatterns("/*");
 *     registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
 *     return registro;
 * }
 * }</pre>
 *
 * <p>En el Gateway reactivo se debe implementar un {@code WebFilter} equivalente usando
 * {@link CorrelationId} y {@link CorrelationIdUtils}; esta librería no incorpora Reactor
 * para no imponer dependencias reactivas a los servicios MVC.</p>
 *
 * <p>Nota: por defecto {@link OncePerRequestFilter} no se ejecuta en despachos asíncronos,
 * por lo que el MDC no se propaga automáticamente a hilos de async. No es un escenario del
 * MVP actual.</p>
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = CorrelationIdUtils.normalizar(request.getHeader(CorrelationId.HEADER));

        request.setAttribute(CorrelationId.ATRIBUTO_REQUEST, correlationId);
        response.setHeader(CorrelationId.HEADER, correlationId);
        CorrelationIdUtils.establecer(correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            CorrelationIdUtils.limpiar();
        }
    }
}
