package com.microservice.inventario.service;

import org.springframework.stereotype.Service;

import com.microservice.inventario.client.PedidoClient;
import com.microservice.inventario.dto.PedidoDTO;
import com.microservice.inventario.http.PedidoByInventarioResponse;
import com.microservice.inventario.model.Inventario;
import com.microservice.inventario.repository.InventarioRepository;

import java.util.List;

/**
 * Servicio con la lógica de negocio
 */
@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final PedidoClient pedidoClient;

    // Inyección por constructor (mejor práctica)
    public InventarioService(InventarioRepository inventarioRepository, PedidoClient pedidoClient) {
        this.inventarioRepository = inventarioRepository;
        this.pedidoClient = pedidoClient;
    }

    // Obtener solo registros activos
    public List<Inventario> findAll() {
        return inventarioRepository.findByActivoTrue();
    }

    // Buscar por ID (solo activos)
    public Inventario findById(Long id) {
        return inventarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
    }

    // Guardar inventario
    public Inventario save(Inventario inventario) {
        return inventarioRepository.save(inventario);
    }

    // Actualizar inventario
    public Inventario update(Long id, Inventario inventario) {
        Inventario existente = findById(id);

        existente.setNombreProducto(inventario.getNombreProducto());
        existente.setUbicacion(inventario.getUbicacion());
        existente.setStock(inventario.getStock());
        existente.setPrecio(inventario.getPrecio());
        existente.setStockMinimo(inventario.getStockMinimo());

        return inventarioRepository.save(existente);
    }

    // Descontar stock — llamado por msvc-pedido via Feign
    public Inventario descontarStock(Long id, Integer cantidad) {
        Inventario inventario = findById(id);
        if (inventario.getStock() < cantidad) {
            throw new RuntimeException(
                "Stock insuficiente para '" + inventario.getNombreProducto() +
                "'. Disponible: " + inventario.getStock()
            );
        }
        inventario.setStock(inventario.getStock() - cantidad);
        return inventarioRepository.save(inventario);
    }

    // Baja lógica — no elimina el registro, cambia activo a false
    public void desactivar(Long id) {
        Inventario inventario = findById(id);
        inventario.setActivo(false);
        inventarioRepository.save(inventario);
    }

    // Consumir microservicio pedido
    public PedidoByInventarioResponse findPedidosByInventarioId(Long inventarioId) {

        Inventario inventario = findById(inventarioId);

        List<PedidoDTO> pedidos = pedidoClient.findAllProductoByInventario(inventarioId);

        return PedidoByInventarioResponse.builder()
                .nombreProducto(inventario.getNombreProducto())
                .ubicacion(inventario.getUbicacion())
                .pedidoList(pedidos)
                .build();
    }
}