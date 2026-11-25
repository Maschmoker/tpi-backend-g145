package com.guarderia.gateway_service.filter;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdWebFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = CorrelationIdUtils.normalizar(
                exchange.getRequest().getHeaders().getFirst(CorrelationId.HEADER));

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(CorrelationId.HEADER, correlationId)
                .build();

        ServerWebExchange mutado = exchange.mutate().request(request).build();
        mutado.getResponse().getHeaders().set(CorrelationId.HEADER, correlationId);

        return chain.filter(mutado);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
