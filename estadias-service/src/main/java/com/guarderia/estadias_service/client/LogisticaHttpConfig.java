package com.guarderia.estadias_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class LogisticaHttpConfig {

    // JdkClientHttpRequestFactory soporta PATCH; SimpleClientHttpRequestFactory
    // (HttpURLConnection) no lo permite y rompia las llamadas internas de habilitacion.
    @Bean("logisticaRestClient")
    public RestClient logisticaRestClient(@Value("${servicios.logistica.url}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
