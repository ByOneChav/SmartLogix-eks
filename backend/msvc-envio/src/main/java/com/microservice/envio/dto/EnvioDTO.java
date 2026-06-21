package com.microservice.envio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO para recibir los datos de creación de un envío desde el frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvioDTO implements Serializable {

    private Long pedidoId;
    private String direccionDestino;
}
