package com.microservice.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Respuesta con JWT
 */
@Data
@Builder
public class AuthResponse {

    @Schema(description = "Token JWT generado", example = "eyJhbGciOiJIUzI1Ni...")
    private String token;
    private UserResponse user;
}