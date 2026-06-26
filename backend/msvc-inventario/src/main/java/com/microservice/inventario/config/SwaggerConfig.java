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
	public OpenAPI customOpenAPI() {

		return new OpenAPI()

				// ANTES
				// .addServersItem(new Server().url("http://localhost:8080"))

				// DESPUÉS
				.addServersItem(new Server().url(
						"http://a89667c0fa57949db96ac7316657d2ca-1585154752.us-east-1.elb.amazonaws.com"))

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
										"""));
	}
}