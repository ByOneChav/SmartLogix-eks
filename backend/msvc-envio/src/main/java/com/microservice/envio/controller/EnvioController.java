package com.microservice.envio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.envio.dto.EnvioDTO;
import com.microservice.envio.model.Envio;
import com.microservice.envio.model.EstadoEnvio;
import com.microservice.envio.service.EnvioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST de Envíos.
 */
@RestController
@RequestMapping("/api/envio")
@Tag(name = "Envio", description = "Gestión de envíos creados a partir de pedidos")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/getAllEnvios")
    @Operation(summary = "Listar todos los envíos")
    public ResponseEntity<List<Envio>> findAll() {
        return ResponseEntity.ok(envioService.findAll());
    }

    @GetMapping("/getEnvio/{id}")
    @Operation(summary = "Buscar envío por ID")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(envioService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/addEnvio")
    @Operation(
        summary = "Crear envío desde un pedido",
        description = "Registra el despacho de un pedido. Estado inicial: PREPARANDO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Envío creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Pedido inválido o ya tiene envío")
    })
    public ResponseEntity<?> crearEnvio(@RequestBody EnvioDTO envio) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(envioService.crearEnvio(envio));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/updEstadoEnvio/{id}")
    @Operation(
        summary = "Cambiar estado del envío",
        description = "Transiciones: PREPARANDO → EN_TRANSITO → ENTREGADO. También puede pasar a DEVUELTO."
    )
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoEnvio estado) {
        try {
            return ResponseEntity.ok(envioService.cambiarEstado(id, estado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/updEnvio/{id}")
    @Operation(summary = "Actualizar dirección de destino del envío")
    public ResponseEntity<?> updateEnvio(@PathVariable Long id, @RequestBody Envio envio) {
        try {
            return ResponseEntity.ok(envioService.update(id, envio));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/delEnvio/{id}")
    @Operation(summary = "Eliminar envío")
    public ResponseEntity<?> deleteEnvio(@PathVariable Long id) {
        try {
            envioService.delete(id);
            return ResponseEntity.ok("Envío eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/getEnviosByPedido/{pedidoId}")
    @Operation(summary = "Buscar envíos por pedidoId")
    public ResponseEntity<List<Envio>> findByPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(envioService.findByPedidoId(pedidoId));
    }
}
