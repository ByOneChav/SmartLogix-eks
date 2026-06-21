package com.microservice.authservice.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración Swagger del AuthService
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                // 🌐 Define el Gateway como punto de entrada (NO el microservicio)
                .addServersItem(new Server().url("http://localhost:8080"))

                // 🔐 Configuración de seguridad JWT para Swagger
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .name("Authorization") // Header esperado
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer") // Tipo Bearer
                                        .bearerFormat("JWT") // Formato JWT
                        )
                )

                // 🔒 Aplica seguridad global (aparece botón "Authorize")
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))

                // 📝 Información de la API
                .info(new Info()
                        .title("Auth Service - SMARTLOGIX")
                        .version("5.0")
                        .description("""
                                Servicio encargado de:
                                - Registro de usuarios
                                - Autenticación (login)
                                - Generación de tokens JWT
                                """));
    }
}