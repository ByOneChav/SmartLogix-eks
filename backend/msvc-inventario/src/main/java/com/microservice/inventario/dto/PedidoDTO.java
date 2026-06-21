package com.microservice.inventario.dto;

import lombok.*;

/**
 * DTO para recibir datos del microservicio student
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PedidoDTO {

    private String descripcion;
    private Integer cantidad;
    private Integer precio;

    private Long inventarioId;
}
