package com.microservice.inventario.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservice.inventario.model.Inventario;
import com.microservice.inventario.service.InventarioService;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Controlador REST de Inventario
 */
@RestController
@RequestMapping("/api/inventario")
@Tag(name = "INVENTARIO", description = "Endpoints para gestionar el inventario: registrar, listar, actualizar, eliminar productos y consultar pedidos asociados.")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * LISTAR INVENTARIO
     */
    @GetMapping("/getAllInventario")
    @Operation(summary = "Listar inventario", description = "Devuelve una lista completa de todos los productos registrados en el inventario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario obtenido correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Inventario.class))),
            @ApiResponse(responseCode = "404", description = "No hay productos en el inventario", content = @Content)
    })
    public ResponseEntity<List<Inventario>> findAll() {
        return ResponseEntity.ok(inventarioService.findAll());
    }

    /**
     * BUSCAR POR ID
     */
    @GetMapping("/getInventario/{id}")
    @Operation(summary = "Buscar inventario por ID", description = "Obtiene la información de un producto específico mediante su ID.")
    @Parameter(name = "id", description = "ID del inventario", required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(schema = @Schema(implementation = Inventario.class))),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(inventarioService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventario no encontrado");
        }
    }

    /**
     * CREAR INVENTARIO
     */
    @PostMapping("/addInventario")
    @Operation(summary = "Crear inventario", description = "Registra un nuevo producto dentro del inventario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventario creado correctamente", content = @Content(schema = @Schema(implementation = Inventario.class))),
            @ApiResponse(responseCode = "400", description = "Error en los datos enviados")
    })
    public ResponseEntity<?> save(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Objeto Inventario a registrar", required = true, content = @Content(schema = @Schema(implementation = Inventario.class))) @RequestBody Inventario inventario) {

        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(inventarioService.save(inventario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear inventario");
        }
    }

    /**
     * ACTUALIZAR INVENTARIO
     */
    @PutMapping("/updInventario/{id}")
    @Operation(summary = "Actualizar inventario", description = "Actualiza los datos de un producto existente en el inventario.")
    @Parameter(name = "id", description = "ID del inventario a actualizar", required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Inventario inventario) {

        try {
            return ResponseEntity.ok(inventarioService.update(id, inventario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo actualizar");
        }
    }

    /**
     * BAJA LÓGICA DE INVENTARIO
     */
    @DeleteMapping("/delInventario/{id}")
    @Operation(summary = "Dar de baja inventario", description = "Desactiva un producto del inventario (baja lógica). El registro se conserva en base de datos con activo=false.")
    @Parameter(name = "id", description = "ID del inventario a desactivar", required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario dado de baja correctamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            inventarioService.desactivar(id);
            return ResponseEntity.ok("Inventario dado de baja correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventario no encontrado");
        }
    }

    /**
     * DESCONTAR STOCK — Consumido por msvc-pedido via Feign
     */
    @PutMapping("/updDescontarStock/{id}")
    @Operation(summary = "Descontar stock", description = "Descuenta la cantidad indicada del stock. Llamado internamente por msvc-pedido via Feign al crear un pedido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock descontado correctamente"),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente")
    })
    public ResponseEntity<?> descontarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        try {
            return ResponseEntity.ok(inventarioService.descontarStock(id, cantidad));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * OBTENER PEDIDOS ASOCIADOS
     */
    @GetMapping("/getPedidosByInventario/{inventarioId}")
    @Operation(summary = "Obtener pedidos por inventario", description = "Consulta los pedidos asociados a un producto del inventario mediante integración con el microservicio de pedidos.")
    @Parameter(name = "inventarioId", description = "ID del inventario", required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos obtenidos correctamente"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    public ResponseEntity<?> findPedidos(@PathVariable Long inventarioId) {
        return ResponseEntity.ok(
                inventarioService.findPedidosByInventarioId(inventarioId));
    }
}