package com.microservice.pedido.TestPedido;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.microservice.pedido.client.InventarioClient;
import com.microservice.pedido.model.EstadoPedido;
import com.microservice.pedido.model.Pedido;
import com.microservice.pedido.repository.PedidoRepository;
import com.microservice.pedido.service.PedidoService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Clase de pruebas unitarias para PedidoService
 * Usa Mockito para simular el repositorio (NO usa BD real)
 */
@ExtendWith(MockitoExtension.class)
public class PruebasTestPedido {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoService pedidoService;

    /**
     * TEST 1: Obtener todos los pedidos
     */
    @Test
    public void testFindAll() {
        Pedido p = Pedido.builder()
                .id(1L)
                .clienteNombre("Juan Pérez")
                .descripcion("Compra PC")
                .cantidad(2)
                .precio(500000)
                .inventarioId(10L)
                .estado(EstadoPedido.PENDIENTE)
                .fechaPedido(LocalDateTime.now())
                .build();

        when(pedidoRepository.findAll()).thenReturn(List.of(p));

        List<Pedido> resultado = pedidoService.findAll();

        assertEquals(1, resultado.size());
        assertEquals("Compra PC", resultado.get(0).getDescripcion());
        verify(pedidoRepository, times(1)).findAll();
    }

    /**
     * TEST 2: Buscar pedido por ID (existe)
     */
    @Test
    public void testFindById() {
        Pedido p = Pedido.builder()
                .id(1L)
                .clienteNombre("Juan Pérez")
                .descripcion("Compra PC")
                .cantidad(2)
                .precio(500000)
                .inventarioId(10L)
                .estado(EstadoPedido.PENDIENTE)
                .build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

        Pedido resultado = pedidoService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Pérez", resultado.getClienteNombre());
        verify(pedidoRepository).findById(1L);
    }

    /**
     * TEST 3: Buscar pedido por ID (NO existe)
     */
    @Test
    public void testFindByIdNotFound() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.findById(99L);
        });

        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository).findById(99L);
    }

    /**
     * TEST 4: Eliminar pedido
     */
    @Test
    public void testDelete() {
        Long id = 1L;
        doNothing().when(pedidoRepository).deleteById(id);

        pedidoService.delete(id);

        verify(pedidoRepository, times(1)).deleteById(id);
    }

    /**
     * TEST 5: Buscar pedidos por inventarioId
     */
    @Test
    public void testFindByInventarioId() {
        Pedido p = Pedido.builder()
                .id(1L)
                .clienteNombre("Juan Pérez")
                .descripcion("Compra PC")
                .cantidad(2)
                .precio(500000)
                .inventarioId(10L)
                .estado(EstadoPedido.PENDIENTE)
                .build();

        when(pedidoRepository.findAllByInventarioId(10L)).thenReturn(List.of(p));

        List<Pedido> resultado = pedidoService.findByInventarioId(10L);

        assertEquals(1, resultado.size());
        verify(pedidoRepository).findAllByInventarioId(10L);
    }

    /**
     * TEST 6: Cambiar estado de pedido
     */
    @Test
    public void testCambiarEstado() {
        Pedido p = Pedido.builder()
                .id(1L)
                .clienteNombre("Juan Pérez")
                .descripcion("Compra PC")
                .cantidad(2)
                .precio(500000)
                .inventarioId(10L)
                .estado(EstadoPedido.PENDIENTE)
                .build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(p);

        Pedido resultado = pedidoService.cambiarEstado(1L, EstadoPedido.CONFIRMADO);

        assertNotNull(resultado);
        verify(pedidoRepository).save(any(Pedido.class));
    }
}
