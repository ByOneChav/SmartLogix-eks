package com.microservice.pedido.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad Pedido.
 * Representa una orden de compra vinculada a un producto del inventario.
 * Estado inicial: PENDIENTE → CONFIRMADO → EN_PREPARACION → ENVIADO → ENTREGADO
 */
@Entity
@Table(name = "pedido")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del pedido", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "cliente_nombre")
    @Schema(description = "Nombre del cliente", example = "Juan Pérez")
    private String clienteNombre;

    @Column(nullable = false)
    @Schema(description = "Descripción del pedido", example = "Compra de Laptop Dell")
    private String descripcion;

    @Column(nullable = false)
    @Schema(description = "Cantidad de productos", example = "2")
    private Integer cantidad;

    @Schema(description = "Precio total (calculado automáticamente)", example = "1000000")
    private Integer precio;

    @Column(name = "inventario_id", nullable = false)
    @Schema(description = "ID del producto en inventario", example = "1")
    private Long inventarioId;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado actual del pedido")
    private EstadoPedido estado;

    @Column(name = "fecha_pedido")
    @Schema(description = "Fecha y hora de creación del pedido")
    private LocalDateTime fechaPedido;
}
