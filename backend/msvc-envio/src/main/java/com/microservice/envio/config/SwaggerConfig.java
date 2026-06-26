package com.microservice.envio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // .addServersItem(new Server().url("http://localhost:8080"))
                // DESPUÉS
				.addServersItem(new Server().url(
						"http://a89667c0fa57949db96ac7316657d2ca-1585154752.us-east-1.elb.amazonaws.com"))
                .info(
                        new Info()
                                .title("Envio Service - SMARTLOGIX")
                                .version("v1.0")
                                .description("""
                                        Servicio encargado de:
                                        - Registro de envío a partir de un pedido
                                        - Seguimiento de estado del envío
                                        - Actualización de dirección de destino
                                        - Eliminación de envío
                                        """));
    }
}
