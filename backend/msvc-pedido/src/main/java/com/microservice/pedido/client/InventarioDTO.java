package com.microservice.pedido.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la respuesta del msvc-inventario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDTO {
    private Long id;
    private String nombreProducto;
    private String ubicacion;
    private Integer stock;
    private Integer stockMinimo;
    private Integer precio;
}
