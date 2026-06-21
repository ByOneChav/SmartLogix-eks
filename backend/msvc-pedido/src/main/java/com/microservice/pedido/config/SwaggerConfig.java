package com.microservice.pedido.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

// 👇 IMPORTANTE: importar Server
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                // 👇 AQUÍ DEFINES EL API GATEWAY COMO PUERTA DE ENTRADA
                .addServersItem(new Server().url("http://localhost:8080"))

                .info(
                        new Info()
                                .title("Pedido Service - SMARTLOGIX")
                                .version("v5.6")
                                .description("""
                                        Servicio encargado de:
                                        - Registro de pedido
                                        - Actualizar pedido
                                        - Buscar pedido
                                        - Eliminar pedido
                                        """));
    }
}