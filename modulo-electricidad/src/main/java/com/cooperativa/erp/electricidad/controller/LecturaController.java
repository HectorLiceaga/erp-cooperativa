package com.cooperativa.erp.controller.electricidad;

import com.cooperativa.erp.controller.electricidad.dto.LecturaDTO;
import com.cooperativa.erp.controller.electricidad.dto.RegistrarLecturaRequestDTO;
import com.cooperativa.erp.electricidad.entity.Lectura; // Import entidad
import com.cooperativa.erp.electricidad.service.LecturaService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/electricidad/lecturas") // Ruta base para lecturas
public class LecturaController {

    private final LecturaService lecturaService;

    public LecturaController(LecturaService lecturaService) {
        this.lecturaService = lecturaService;
    }

    @PostMapping
    public ResponseEntity<LecturaDTO> registrarLectura(@Valid @RequestBody RegistrarLecturaRequestDTO requestDTO) {
        try {
            Lectura nuevaLectura = lecturaService.registrarLectura(
                    requestDTO.getMedidorId(),
                    requestDTO.getFechaLectura(),
                    requestDTO.getEstado(),
                    requestDTO.getTipoLectura()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirALecturaDTO(nuevaLectura));
        } catch (Exception e) {
            // Manejo básico de errores, idealmente con @ControllerAdvice y excepciones personalizadas
            if (e.getMessage().contains("Medidor no encontrado")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else if (e.getMessage().contains("fecha de la nueva lectura debe ser posterior") || e.getMessage().contains("estado de la nueva lectura")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); // Error de validación de negocio
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al registrar lectura: " + e.getMessage());
        }
    }

    // --- Método Helper ---
    private LecturaDTO convertirALecturaDTO(Lectura lectura) {
        LecturaDTO dto = new LecturaDTO();
        BeanUtils.copyProperties(lectura, dto);
        if (lectura.getMedidor() != null) {
            dto.setMedidorId(lectura.getMedidor().getId());
            dto.setMedidorNumero(lectura.getMedidor().getNumero());
        }
        return dto;
    }
}

