package com.guarderia.clientes_service.config;

import com.guarderia.commons.correlacion.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registra el filtro de {@code X-Correlation-ID} antes de la cadena de Spring Security,
 * de modo que el MDC ya esté poblado cuando el filtro de seguridad registre errores.
 */
@Configuration
public class CorrelationIdConfig {

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registro = new FilterRegistrationBean<>(new CorrelationIdFilter());
        registro.setName("correlationIdFilter");
        registro.addUrlPatterns("/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}
