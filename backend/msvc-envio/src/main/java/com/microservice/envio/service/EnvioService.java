package com.microservice.envio.service;

import java.time.LocalDateTime;
import java.util.List;

import com.microservice.envio.dto.EnvioDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microservice.envio.client.PedidoClient;
import com.microservice.envio.model.Envio;
import com.microservice.envio.model.EstadoEnvio;
import com.microservice.envio.repository.EnvioRepository;

import jakarta.transaction.Transactional;

/**
 * Servicio de negocio para Envíos.
 * Coordina estados con msvc-pedido via Feign:
 *   - crearEnvio  → pedido pasa a ENVIADO
 *   - ENTREGADO   → pedido pasa a ENTREGADO
 *   - DEVUELTO    → pedido pasa a EN_PREPARACION (para re-despachar)
 */
@Service
@Transactional
public class EnvioService {

    private static final Logger log = LoggerFactory.getLogger(EnvioService.class);

    private final EnvioRepository envioRepository;
    private final PedidoClient pedidoClient;

    public EnvioService(EnvioRepository envioRepository, PedidoClient pedidoClient) {
        this.envioRepository = envioRepository;
        this.pedidoClient = pedidoClient;
    }

    public List<Envio> findAll() {
        return envioRepository.findAll();
    }

    public Envio findById(Long id) {
        return envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envio no encontrado con ID: " + id));
    }

    public Envio crearEnvio(EnvioDTO dto) {
        if (dto.getPedidoId() == null) {
            throw new RuntimeException("El pedidoId es obligatorio para crear un envio");
        }
        if (dto.getDireccionDestino() == null || dto.getDireccionDestino().isBlank()) {
            throw new RuntimeException("La dirección de destino es obligatoria");
        }
        if (envioRepository.findByPedidoId(dto.getPedidoId()).isPresent()) {
            throw new RuntimeException("Ya existe un envio para el pedido ID: " + dto.getPedidoId());
        }

        Envio envio = Envio.builder()
                .pedidoId(dto.getPedidoId())
                .direccionDestino(dto.getDireccionDestino())
                .estado(EstadoEnvio.PREPARANDO)
                .fechaEnvio(LocalDateTime.now())
                .build();

        Envio guardado = envioRepository.save(envio);

        // Notificar al pedido que ya fue enviado
        try {
            pedidoClient.cambiarEstado(dto.getPedidoId(), "ENVIADO");
        } catch (Exception e) {
            log.warn("No se pudo actualizar el estado del pedido {}: {}", dto.getPedidoId(), e.getMessage());
        }

        return guardado;
    }

    public Envio cambiarEstado(Long id, EstadoEnvio nuevoEstado) {
        Envio envio = findById(id);
        envio.setEstado(nuevoEstado);
        Envio guardado = envioRepository.save(envio);

        // Sincronizar estado del pedido cuando el envío se entrega o devuelve
        try {
            if (nuevoEstado == EstadoEnvio.ENTREGADO) {
                pedidoClient.cambiarEstado(envio.getPedidoId(), "ENTREGADO");
            } else if (nuevoEstado == EstadoEnvio.DEVUELTO) {
                pedidoClient.cambiarEstado(envio.getPedidoId(), "EN_PREPARACION");
            }
        } catch (Exception e) {
            log.warn("No se pudo sincronizar el estado del pedido {}: {}", envio.getPedidoId(), e.getMessage());
        }

        return guardado;
    }

    public Envio update(Long id, Envio envio) {
        Envio existente = findById(id);
        existente.setDireccionDestino(envio.getDireccionDestino());
        return envioRepository.save(existente);
    }

    public void delete(Long id) {
        envioRepository.deleteById(id);
    }

    public List<Envio> findByPedidoId(Long pedidoId) {
        return envioRepository.findAllByPedidoId(pedidoId);
    }
}
