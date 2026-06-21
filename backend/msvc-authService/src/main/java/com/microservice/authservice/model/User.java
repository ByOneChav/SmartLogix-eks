package com.microservice.authservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Usuario
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa un usuario del sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del usuario", example = "Juan Pérez")
    private String name;

    @Column(unique = true)
    @Schema(description = "Email único del usuario", example = "juan@gmail.com")
    private String email;

    @Schema(description = "Contraseña encriptada", example = "******")
    private String password;

    @Schema(description = "Rol del usuario", example = "USER")
    private String role;
}
