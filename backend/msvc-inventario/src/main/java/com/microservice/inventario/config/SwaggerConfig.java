package com.microservice.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuración de Swagger/OpenAPI para el microservicio de Inventario
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(){

        return new OpenAPI()

                // 🌐 API Gateway como punto de entrada (igual que Pedido)
                .addServersItem(new Server().url("http://localhost:8080"))

                // 📌 Información del microservicio
                .info(
                        new Info()
                                .title("Inventario Service - SMARTLOGIX")
                                .version("v5.6")
                                .description("""
                                        Servicio encargado de:
                                        - Registro de inventario
                                        - Actualizar inventario
                                        - Buscar inventario
                                        - Eliminar inventario
                                        """)
                );
    }
}