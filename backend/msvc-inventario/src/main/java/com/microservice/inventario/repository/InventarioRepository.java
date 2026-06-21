package com.microservice.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservice.inventario.model.Inventario;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Inventario
 * Permite acceso a la base de datos
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByActivoTrue();

    Optional<Inventario> findByIdAndActivoTrue(Long id);
}