package com.microservice.envio.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla envio
 */
@Entity
@Table(name = "envio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "direccion_destino", nullable = false)
    private String direccionDestino;

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estado;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;
}
