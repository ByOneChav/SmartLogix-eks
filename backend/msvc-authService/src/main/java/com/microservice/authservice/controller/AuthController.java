package com.microservice.authservice.controller;

import com.microservice.authservice.dto.*;
import com.microservice.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticación y gestión de usuarios")
public class AuthController {

    private final AuthService authService;

    /**
     * 🔐 Registro de usuario
     */
    @Operation(
            summary = "Registrar usuario",
            description = "Permite registrar un nuevo usuario en el sistema y retorna un JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * 🔐 Login
     */
    @Operation(
            summary = "Login usuario",
            description = "Autentica un usuario y retorna un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
}