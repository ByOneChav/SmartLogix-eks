package com.microservice.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO para registro
 */
@Data
public class RegisterRequest {

    @Schema(description = "Nombre del usuario", example = "Juan Pérez")
    private String name;

    @Schema(description = "Correo electrónico", example = "juan@gmail.com")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "123456")
    private String password;

    @Schema(description = "ROL", example = "USER")
    private String rol;
}