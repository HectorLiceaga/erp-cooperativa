package com.cooperativa.erp.electricidad.controller;

import com.cooperativa.erp.controller.electricidad.dto.LecturaDTO;
import com.cooperativa.erp.controller.electricidad.dto.RegistrarLecturaRequestDTO;
import com.cooperativa.erp.electricidad.entity.Lectura;
import com.cooperativa.erp.electricidad.service.LecturaService;
import jakarta.validation.Valid; // Asegúrate que esté este import para @Valid
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Ya no necesitamos este import

@RestController
@RequestMapping("/api/electricidad/lecturas")
public class LecturaController {

    private static final Logger log = LoggerFactory.getLogger(LecturaController.class);
    private final LecturaService lecturaService;

    public LecturaController(LecturaService lecturaService) {
        this.lecturaService = lecturaService;
    }

    /**
     * Endpoint para registrar una nueva lectura.
     * Recibe los datos de la lectura en el cuerpo de la petición.
     * Delega la lógica de negocio (incluidas validaciones) al LecturaService.
     *
     * @param requestDTO DTO con los datos de la lectura a registrar.
     * @return ResponseEntity con el DTO de la lectura creada o un error.
     */
    @PostMapping
    public ResponseEntity<LecturaDTO> registrarLectura(@Valid @RequestBody RegistrarLecturaRequestDTO requestDTO) {
        log.info("Recibida petición para registrar lectura: {}", requestDTO);

        // SIMPLEMENTE LLAMAMOS AL SERVICIO. Si lanza IllegalArgumentException,
        // el RestExceptionHandler lo capturará y devolverá 400.
        // Si lanza otra excepción inesperada, RestExceptionHandler devolverá 500.
        Lectura nuevaLectura = lecturaService.registrarLectura(
                requestDTO.getMedidorId(),
                requestDTO.getFechaLectura(),
                requestDTO.getEstado(),
                requestDTO.getTipoLectura()
        );

        // Si llegamos aquí, la lectura se guardó correctamente. Convertimos a DTO y devolvemos 200 OK.
        LecturaDTO responseDTO = new LecturaDTO(
                nuevaLectura.getId(),
                nuevaLectura.getMedidor().getId(),
                nuevaLectura.getMedidor().getNumero(), // Añadimos el número para conveniencia
                nuevaLectura.getFechaLectura(),
                nuevaLectura.getEstado(),
                nuevaLectura.getTipoLectura()
        );
        log.info("Lectura registrada exitosamente: {}", responseDTO);
        return ResponseEntity.ok(responseDTO);

        // EL TRY-CATCH ANTERIOR SE ELIMINA COMPLETAMENTE
    }

    // Otros endpoints para lecturas (GET, PUT, DELETE) podrían ir aquí...
}


