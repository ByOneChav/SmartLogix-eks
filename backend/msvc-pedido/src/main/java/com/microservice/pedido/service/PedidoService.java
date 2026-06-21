package com.microservice.pedido.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.microservice.pedido.client.InventarioClient;
import com.microservice.pedido.client.InventarioDTO;
import com.microservice.pedido.model.EstadoPedido;
import com.microservice.pedido.model.Pedido;
import com.microservice.pedido.repository.PedidoRepository;

import jakarta.transaction.Transactional;

/**
 * Servicio de negocio para Pedidos.
 * Flujo principal al crear un pedido:
 *  1. Consulta el inventario via Feign para validar stock.
 *  2. Calcula el precio total si no fue enviado.
 *  3. Descuenta el stock en inventario via Feign.
 *  4. Guarda el pedido con estado PENDIENTE.
 */
@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;

    public PedidoService(PedidoRepository pedidoRepository, InventarioClient inventarioClient) {
        this.pedidoRepository = pedidoRepository;
        this.inventarioClient = inventarioClient;
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Pedido findById(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public Pedido crearPedido(Pedido pedido) {
        InventarioDTO inventario = inventarioClient.findById(pedido.getInventarioId());

        if (inventario.getStock() < pedido.getCantidad()) {
            throw new RuntimeException(
                "Stock insuficiente para '" + inventario.getNombreProducto() + "'. " +
                "Disponible: " + inventario.getStock() + ", solicitado: " + pedido.getCantidad()
            );
        }

        if (pedido.getPrecio() == null || pedido.getPrecio() == 0) {
            pedido.setPrecio(inventario.getPrecio() * pedido.getCantidad());
        }

        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());

        inventarioClient.descontarStock(pedido.getInventarioId(), pedido.getCantidad());

        return pedidoRepository.save(pedido);
    }

    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = findById(id);
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    public void delete(Long id) {
        pedidoRepository.deleteById(id);
    }

    public List<Pedido> findByInventarioId(Long inventarioId) {
        return pedidoRepository.findAllByInventarioId(inventarioId);
    }
}
