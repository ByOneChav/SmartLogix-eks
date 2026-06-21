package com.microservice.pedido.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente Feign para consumir msvc-inventario.
 * Permite validar y descontar stock antes de crear un pedido.
 */
@FeignClient(name = "msvc-inventario", url = "${msvc-inventario.url:}")
public interface InventarioClient {

    @GetMapping("/api/inventario/getInventario/{id}")
    InventarioDTO findById(@PathVariable("id") Long id);

    @PutMapping("/api/inventario/updDescontarStock/{id}")
    void descontarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad);
}
