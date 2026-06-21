package com.microservice.inventario.http;

import lombok.*;

import java.util.List;

import com.microservice.inventario.dto.PedidoDTO;

/**
 * Respuesta combinada (curso + estudiantes)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PedidoByInventarioResponse {

    private String nombreProducto;
    private String ubicacion;
    private List<PedidoDTO> pedidoList;
}
