package com.microservice.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.Arrays;

/**
 * Configuración CORS para el API Gateway
 * Permite que Swagger (u otros clientes) puedan consumir los endpoints
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsWebFilter corsWebFilter() {

        // Configuración CORS
        CorsConfiguration config = new CorsConfiguration();

        Arrays.stream(allowedOrigins.split(","))
              .map(String::trim)
              .forEach(config::addAllowedOrigin);

        // Permitir todos los métodos HTTP
        config.addAllowedMethod("*");

        // Permitir todos los headers
        config.addAllowedHeader("*");

        // Permitir credenciales (tokens, auth, etc)
        config.setAllowCredentials(true);

        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
