package com.microservice.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservice.pedido.model.Pedido;

/**
 * Repositorio que conecta con la BD
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Query derivada para buscar por inventarioId
    List<Pedido> findAllByInventarioId(Long inventarioId);
}