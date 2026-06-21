package com.microservice.pedido.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.pedido.model.EstadoPedido;
import com.microservice.pedido.model.Pedido;
import com.microservice.pedido.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST de Pedidos.
 */
@RestController
@RequestMapping("/api/pedido")
@Tag(name = "Pedido", description = "Gestión de pedidos con validación de stock via Feign")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/getAllPedidos")
    @Operation(summary = "Listar todos los pedidos")
    public ResponseEntity<List<Pedido>> findAll() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @GetMapping("/getPedido/{id}")
    @Operation(summary = "Buscar pedido por ID")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pedidoService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/addPedido")
    @Operation(
        summary = "Crear pedido con validación de stock",
        description = "Consulta inventario via Feign, valida stock, lo descuenta y registra el pedido en estado PENDIENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pedido creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos inválidos")
    })
    public ResponseEntity<?> crearPedido(@RequestBody Pedido pedido) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pedidoService.crearPedido(pedido));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/updEstadoPedido/{id}")
    @Operation(
        summary = "Cambiar estado del pedido",
        description = "Transiciones: PENDIENTE → CONFIRMADO → EN_PREPARACION → ENVIADO → ENTREGADO. Cancelar: → CANCELADO."
    )
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido estado) {
        try {
            return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delPedido/{id}")
    @Operation(summary = "Eliminar pedido")
    public ResponseEntity<?> deletePedido(@PathVariable Long id) {
        try {
            pedidoService.delete(id);
            return ResponseEntity.ok("Pedido eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/getPedidosByInventario/{inventarioId}")
    @Operation(summary = "Buscar pedidos por inventarioId")
    public ResponseEntity<List<Pedido>> findByInventario(@PathVariable Long inventarioId) {
        return ResponseEntity.ok(pedidoService.findByInventarioId(inventarioId));
    }
}
