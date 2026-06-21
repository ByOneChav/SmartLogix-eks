package com.microservice.authservice.service;

import com.microservice.authservice.dto.*;
import com.microservice.authservice.model.User;
import com.microservice.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Lógica de autenticación
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Registro
    public AuthResponse register(RegisterRequest request) {

    System.out.println("PASO 1");

    User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRol())
        .build();

    System.out.println("PASO 2");

    userRepository.save(user);

    System.out.println("PASO 3");

    String token = jwtService.generateToken(user.getEmail());

    System.out.println("PASO 4");

    return AuthResponse.builder()
        .token(token)
        .user(
            UserResponse.builder()
                .id(user.getId())
                .nombre(user.getName())
                .email(user.getEmail())
                .rol(user.getRole())
                .build()
        )
        .build();
}

    // Login
    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return AuthResponse.builder()
                .token(jwtService.generateToken(user.getEmail()))
                .user(
                        UserResponse.builder()
                                .id(user.getId())
                                .nombre(user.getName())
                                .email(user.getEmail())
                                .rol(user.getRole())
                                .build()
                )
                .build();
    }
}