package com.cooperativa.erp.contabilidad.controller;

import com.cooperativa.erp.contabilidad.dto.AsientoDTO;
import com.cooperativa.erp.contabilidad.service.AsientoService;
import jakarta.validation.Valid;
// --- IMPORTACIONES AÑADIDAS ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// --- FIN IMPORTACIONES ---
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contabilidad/asientos")
public class AsientoController {

    // --- CAMPO LOGGER AÑADIDO ---
    private static final Logger log = LoggerFactory.getLogger(AsientoController.class);
    // --- FIN CAMPO LOGGER ---

    private final AsientoService asientoService;

    public AsientoController(AsientoService asientoService) {
        this.asientoService = asientoService;
    }

    /**
     * POST /api/v1/contabilidad/asientos
     * Registra un nuevo asiento manual.
     */
    @PostMapping
    // TODO: @PreAuthorize("hasRole('ROLE_CONTABILIDAD')")
    public ResponseEntity<AsientoDTO> registrarAsiento(@Valid @RequestBody AsientoDTO asientoDTO) {
        try {
            AsientoDTO asientoGuardado = asientoService.registrarAsientoManual(asientoDTO);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(asientoGuardado.getId())
                    .toUri();

            return ResponseEntity.created(location).body(asientoGuardado);
        } catch (IllegalArgumentException e) {
            // Error de negocio (ej. no balancea, cuenta no imputable)
            log.warn("Error al registrar asiento: {}", e.getMessage()); // <-- Esto ahora funcionará
            // Devolvemos el mensaje de error de negocio al cliente
            return ResponseEntity.badRequest().body(null); // (Mejorar con un DTO de Error)
        }
    }

    /**
     * GET /api/v1/contabilidad/asientos/{id}
     * Obtiene un asiento por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AsientoDTO> getAsiento(@PathVariable Long id) {
        return asientoService.getAsientoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/contabilidad/asientos/buscar
     * Busca asientos por rango de fechas.
     * Ej: /buscar?desde=2025-01-01&hasta=2025-01-31
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<AsientoDTO>> buscarAsientosPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<AsientoDTO> asientos = asientoService.buscarAsientosPorFechas(desde, hasta);
        return ResponseEntity.ok(asientos);
    }
}

