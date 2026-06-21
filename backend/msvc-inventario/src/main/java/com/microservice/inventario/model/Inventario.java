package com.microservice.inventario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa la tabla inventario
 */
@Entity
@Table(name = "inventario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    // ID autogenerado
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del inventario", example = "1")
    private Long id;

    // Nombre del producto
    @Column(nullable = false)
    @Schema(description = "Nombre del producto", example = "Laptop Lenovo")
    private String nombreProducto;

    // Ubicación física
    @Column(nullable = false)
    @Schema(description = "Ubicación del producto", example = "Bodega A")
    private String ubicacion;

    // Stock disponible
    @Column(nullable = false)
    @Schema(description = "Cantidad disponible", example = "50")
    private Integer stock;

    // Precio unitario
    @Column(nullable = false)
    @Schema(description = "Precio del producto", example = "500000")
    private Integer precio;

    // Stock mínimo para alertas de reposición
    @Column(name = "stock_minimo")
    @Schema(description = "Stock mínimo de alerta. Se resalta en rojo cuando stock <= stockMinimo", example = "5")
    private Integer stockMinimo;

    // Estado lógico — false indica que fue dado de baja (soft delete)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    @Schema(description = "Estado del producto. false = dado de baja", example = "true")
    private Boolean activo = true;
}