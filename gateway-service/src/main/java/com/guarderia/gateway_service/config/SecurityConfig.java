package com.guarderia.gateway_service.config;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import com.guarderia.commons.error.ApiError;
import com.guarderia.commons.error.ApiErrorFactory;
import com.guarderia.commons.security.KeycloakRealmRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                Flux.fromIterable(new KeycloakRealmRoleConverter().convert(jwt)));
        return converter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                       ReactiveJwtAuthenticationConverter jwtConverter,
                                                       JsonMapper jsonMapper) {
        ServerAuthenticationEntryPoint entryPoint = (exchange, ex) ->
                escribirError(exchange, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                        "Token JWT ausente o inválido", jsonMapper);

        ServerAccessDeniedHandler deniedHandler = (exchange, ex) ->
                escribirError(exchange, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                        "No tiene permisos para este recurso", jsonMapper);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health/**").permitAll()
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler)
                )
                .build();
    }

    private Mono<Void> escribirError(ServerWebExchange exchange, HttpStatus status,
                                    String codigo, String mensaje, JsonMapper jsonMapper) {
        String correlationId = CorrelationIdUtils.normalizar(
                exchange.getRequest().getHeaders().getFirst(CorrelationId.HEADER));

        ApiError error = ApiErrorFactory.crear(
                status, codigo, mensaje,
                exchange.getRequest().getPath().value(), correlationId);

        byte[] body;
        try {
            body = jsonMapper.writeValueAsBytes(error);
        } catch (Exception e) {
            body = ("{\"status\":" + status.value() + "}").getBytes();
        }

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(CorrelationId.HEADER, correlationId);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}
