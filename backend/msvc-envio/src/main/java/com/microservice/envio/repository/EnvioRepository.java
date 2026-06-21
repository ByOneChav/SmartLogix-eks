package com.microservice.envio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservice.envio.model.Envio;

/**
 * Repositorio JPA para Envio.
 */
@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {

    List<Envio> findAllByPedidoId(Long pedidoId);

    Optional<Envio> findByPedidoId(Long pedidoId);
}
