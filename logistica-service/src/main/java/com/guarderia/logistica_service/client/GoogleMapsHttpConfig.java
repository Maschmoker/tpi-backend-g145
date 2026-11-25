package com.guarderia.logistica_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class GoogleMapsHttpConfig {
    @Bean("googleMapsRestClient")
    public RestClient googleMapsRestClient(
            @Value("${google.maps.timeouts.connect-timeout-ms:5000}") int connectMs,
            @Value("${google.maps.timeouts.read-timeout-ms:10000}") int readMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectMs));
        factory.setReadTimeout(Duration.ofMillis(readMs));
        return RestClient.builder().requestFactory(factory).build();
    }
}