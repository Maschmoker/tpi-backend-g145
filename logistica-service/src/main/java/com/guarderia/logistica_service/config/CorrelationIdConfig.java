package com.guarderia.logistica_service.config;

import com.guarderia.commons.correlacion.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

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
