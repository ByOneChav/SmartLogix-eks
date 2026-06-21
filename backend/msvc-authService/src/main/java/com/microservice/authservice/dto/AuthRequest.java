package com.microservice.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO para login
 */
@Data
public class AuthRequest {

    @Schema(description = "Correo electrónico del usuario", example = "juan@gmail.com")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "123456")
    private String password;
}