package com.microservice.authService.TestAuthService;

import com.microservice.authservice.dto.*;
import com.microservice.authservice.model.User;
import com.microservice.authservice.repository.UserRepository;
import com.microservice.authservice.service.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de AuthService
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    /**
     * TEST 1: Login exitoso
     */
    @Test
    void testLoginExitoso() {
        AuthRequest request = new AuthRequest();
        request.setEmail("rolando@mail.com");
        request.setPassword("r123456");

        User user = User.builder()
                .id(1L)
                .name("Rolando")
                .email("rolando@mail.com")
                .password("encodedPassword")
                .role("USER")
                .build();

        when(userRepository.findByEmail("rolando@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("r123456", "encodedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken("rolando@mail.com"))
                .thenReturn("jwt-token-simulado");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-simulado", response.getToken());
        assertEquals("rolando@mail.com", response.getUser().getEmail());
        assertEquals("Rolando", response.getUser().getNombre());
        verify(userRepository).findByEmail("rolando@mail.com");
        verify(passwordEncoder).matches("r123456", "encodedPassword");
    }

    /**
     * TEST 2: Login con usuario no encontrado
     */
    @Test
    void testLoginUsuarioNoEncontrado() {
        AuthRequest request = new AuthRequest();
        request.setEmail("noexiste@mail.com");
        request.setPassword("1234");

        when(userRepository.findByEmail("noexiste@mail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findByEmail("noexiste@mail.com");
    }

    /**
     * TEST 3: Login con contraseña incorrecta
     */
    @Test
    void testLoginPasswordIncorrecta() {
        AuthRequest request = new AuthRequest();
        request.setEmail("rolando@mail.com");
        request.setPassword("wrongpassword");

        User user = User.builder()
                .id(1L)
                .name("Rolando")
                .email("rolando@mail.com")
                .password("encodedPassword")
                .role("USER")
                .build();

        when(userRepository.findByEmail("rolando@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    /**
     * TEST 4: Registro exitoso
     */
    @Test
    void testRegistroExitoso() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Nuevo Usuario");
        request.setEmail("nuevo@mail.com");
        request.setPassword("pass123");
        request.setRol("USER");

        User userGuardado = User.builder()
                .id(2L)
                .name("Nuevo Usuario")
                .email("nuevo@mail.com")
                .password("encodedPass")
                .role("USER")
                .build();

        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(userGuardado);
        when(jwtService.generateToken("nuevo@mail.com")).thenReturn("jwt-nuevo-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-nuevo-token", response.getToken());
        assertEquals("nuevo@mail.com", response.getUser().getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("pass123");
    }
}
