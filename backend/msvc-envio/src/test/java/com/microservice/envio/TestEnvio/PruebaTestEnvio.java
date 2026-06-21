package com.microservice.envio.TestEnvio;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import com.microservice.envio.controller.EnvioController;
import com.microservice.envio.dto.EnvioDTO;
import com.microservice.envio.model.Envio;
import com.microservice.envio.model.EstadoEnvio;
import com.microservice.envio.service.EnvioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EnvioController.class)
public class PruebaTestEnvio {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvioService envioService;

    @Test
    void findAllReturnsEnvios() throws Exception {
        Envio envio = Envio.builder()
                .id(1L)
                .pedidoId(10L)
                .direccionDestino("Av. Providencia 1234, Santiago")
                .estado(EstadoEnvio.PREPARANDO)
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(envioService.findAll()).thenReturn(List.of(envio));

        mockMvc.perform(get("/api/envio/getAllEnvios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].pedidoId").value(10L))
                .andExpect(jsonPath("$[0].direccionDestino").value("Av. Providencia 1234, Santiago"))
                .andExpect(jsonPath("$[0].estado").value("PREPARANDO"));

        verify(envioService).findAll();
    }

    @Test
    void findByIdReturnsEnvio() throws Exception {
        Envio envio = Envio.builder()
                .id(1L)
                .pedidoId(10L)
                .direccionDestino("Av. Providencia 1234, Santiago")
                .estado(EstadoEnvio.PREPARANDO)
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(envioService.findById(1L)).thenReturn(envio);

        mockMvc.perform(get("/api/envio/getEnvio/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.pedidoId").value(10L));

        verify(envioService).findById(1L);
    }

    @Test
    void crearEnvioReturnsCreated() throws Exception {
        Envio guardado = Envio.builder()
                .id(1L)
                .pedidoId(10L)
                .direccionDestino("Av. Providencia 1234, Santiago")
                .estado(EstadoEnvio.PREPARANDO)
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(envioService.crearEnvio(any(EnvioDTO.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/envio/addEnvio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pedidoId": 10,
                                  "direccionDestino": "Av. Providencia 1234, Santiago"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.pedidoId").value(10L))
                .andExpect(jsonPath("$.estado").value("PREPARANDO"));

        verify(envioService).crearEnvio(any(EnvioDTO.class));
    }

    @Test
    void cambiarEstadoReturnsOk() throws Exception {
        Envio envio = Envio.builder()
                .id(1L)
                .pedidoId(10L)
                .direccionDestino("Av. Providencia 1234, Santiago")
                .estado(EstadoEnvio.EN_TRANSITO)
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(envioService.cambiarEstado(1L, EstadoEnvio.EN_TRANSITO)).thenReturn(envio);

        mockMvc.perform(put("/api/envio/updEstadoEnvio/1")
                        .param("estado", "EN_TRANSITO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_TRANSITO"));

        verify(envioService).cambiarEstado(1L, EstadoEnvio.EN_TRANSITO);
    }

    @Test
    void deleteEnvioReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/envio/delEnvio/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Envío eliminado correctamente"));

        verify(envioService).delete(1L);
    }
}
