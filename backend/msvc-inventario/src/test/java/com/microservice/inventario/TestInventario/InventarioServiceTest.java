package com.microservice.inventario.TestInventario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.microservice.inventario.model.Inventario;
import com.microservice.inventario.repository.InventarioRepository;
import com.microservice.inventario.service.InventarioService;
import com.microservice.inventario.client.PedidoClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class InventarioServiceTest {

    @Mock
    private InventarioRepository repository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private InventarioService service;

    private Inventario inventario;

    @BeforeEach
    void setup() {
        inventario = Inventario.builder()
                .id(1L)
                .nombreProducto("Laptop")
                .ubicacion("Bodega")
                .stock(10)
                .precio(500000)
                .stockMinimo(5)
                .activo(true)
                .build();
    }

    // 1. Listar (solo activos)
    @Test
    void testFindAll() {
        when(repository.findByActivoTrue()).thenReturn(List.of(inventario));

        List<Inventario> lista = service.findAll();

        assertEquals(1, lista.size());
        assertEquals("Laptop", lista.get(0).getNombreProducto());
        verify(repository).findByActivoTrue();
    }

    // 2. Buscar por ID (solo activos)
    @Test
    void testFindById() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(inventario));

        Inventario result = service.findById(1L);

        assertEquals("Laptop", result.getNombreProducto());
        verify(repository).findByIdAndActivoTrue(1L);
    }

    // 3. Guardar
    @Test
    void testSave() {
        when(repository.save(inventario)).thenReturn(inventario);

        Inventario result = service.save(inventario);

        assertEquals(1L, result.getId());
        verify(repository).save(inventario);
    }

    // 4. Actualizar
    @Test
    void testUpdate() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(inventario));
        when(repository.save(any())).thenReturn(inventario);

        Inventario actualizado = service.update(1L, inventario);

        assertEquals("Laptop", actualizado.getNombreProducto());
        verify(repository).save(any());
    }

    // 5. Baja lógica (desactivar)
    @Test
    void testDesactivar() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(inventario));
        when(repository.save(any())).thenReturn(inventario);

        service.desactivar(1L);

        assertFalse(inventario.getActivo());
        verify(repository).save(inventario);
    }

    // 6. Error cuando no existe
    @Test
    void testFindByIdNotFound() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findById(1L));
    }

    // 7. Descontar stock
    @Test
    void testDescontarStock() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(inventario));
        when(repository.save(any())).thenReturn(inventario);

        Inventario result = service.descontarStock(1L, 3);

        assertEquals(7, inventario.getStock());
        verify(repository).save(any());
    }

    // 8. Descontar stock insuficiente
    @Test
    void testDescontarStockInsuficiente() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(inventario));

        assertThrows(RuntimeException.class, () -> service.descontarStock(1L, 100));
    }

    // 9. Integración con pedido
    @Test
    void testFindPedidos() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(inventario));
        when(pedidoClient.findAllProductoByInventario(1L)).thenReturn(new ArrayList<>());

        var response = service.findPedidosByInventarioId(1L);

        assertEquals("Laptop", response.getNombreProducto());
    }
}
