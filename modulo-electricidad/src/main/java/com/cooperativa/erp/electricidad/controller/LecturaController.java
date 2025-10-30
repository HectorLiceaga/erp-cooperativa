package com.cooperativa.erp.electricidad.controller;

import com.cooperativa.erp.electricidad.dto.LecturaRequestDTO;
import com.cooperativa.erp.electricidad.dto.LecturaResponseDTO;
import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.electricidad.service.LecturaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Asumiendo que tenés validaciones (si no, borrá el @Valid)
// import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de Lecturas.
 * CORREGIDO: Usa DTOs y llama al servicio con los 5 parámetros.
 */
@RestController
@RequestMapping("/api/electricidad/lecturas")
public class LecturaController {

    private final LecturaService lecturaService;

    @Autowired
    public LecturaController(LecturaService lecturaService) {
        this.lecturaService = lecturaService;
    }

    /**
     * Endpoint para registrar una nueva lectura.
     * Recibe el DTO por JSON.
     */
    @PostMapping
    public ResponseEntity<LecturaResponseDTO> registrarNuevaLectura(/*@Valid*/ @RequestBody LecturaRequestDTO requestDTO) {
        try {
            // 1. Llamada al servicio (el que está en el Canvas) con los 5 parámetros
            Lectura nuevaLectura = lecturaService.registrarLectura(
                    requestDTO.getContratoId(),
                    requestDTO.getEstadoActual(),
                    requestDTO.getFechaToma(),
                    requestDTO.getPeriodo(),
                    requestDTO.getTipoLectura()
            );

            // 2. Mapear la entidad a la respuesta DTO (esto fija los getters)
            LecturaResponseDTO responseDTO = LecturaResponseDTO.fromEntity(nuevaLectura);

            // Devolvemos 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (IllegalArgumentException e) {
            // Si el servicio lanza una excepción (ej. "estado menor"), devolvemos 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // Error genérico del servidor
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al registrar la lectura", e);
        }
    }

    // Aquí irían los otros endpoints (GET, etc.)
}
