package com.cooperativa.erp.contabilidad.controller;

import com.cooperativa.erp.contabilidad.dto.PlanDeCuentasDTO;
import com.cooperativa.erp.contabilidad.service.PlanDeCuentasService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contabilidad/plan-de-cuentas")
public class PlanDeCuentasController {

    private final PlanDeCuentasService planDeCuentasService;

    public PlanDeCuentasController(PlanDeCuentasService planDeCuentasService) {
        this.planDeCuentasService = planDeCuentasService;
    }

    /**
     * GET /api/v1/contabilidad/plan-de-cuentas
     * Obtiene el árbol de cuentas completo.
     */
    @GetMapping
    public ResponseEntity<List<PlanDeCuentasDTO>> getArbolCompleto() {
        return ResponseEntity.ok(planDeCuentasService.getArbolDeCuentasCompleto());
    }

    /**
     * GET /api/v1/contabilidad/plan-de-cuentas/{id}
     * Obtiene una cuenta y su sub-árbol.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlanDeCuentasDTO> getCuentaPorId(@PathVariable Long id) {
        return planDeCuentasService.getCuentaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/contabilidad/plan-de-cuentas
     * Crea una nueva cuenta.
     */
    @PostMapping
    // TODO: @PreAuthorize("hasRole('ROLE_CONTABILIDAD')")
    public ResponseEntity<PlanDeCuentasDTO> crearCuenta(@Valid @RequestBody PlanDeCuentasDTO cuentaDTO) {
        try {
            PlanDeCuentasDTO nuevaCuenta = planDeCuentasService.crearCuenta(cuentaDTO);

            // Devuelve 201 Created con la URL al nuevo recurso
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(nuevaCuenta.getId())
                    .toUri();

            return ResponseEntity.created(location).body(nuevaCuenta);
        } catch (IllegalArgumentException e) {
            // Error de negocio (ej. padre imputable, código duplicado)
            return ResponseEntity.badRequest().body(null); // (Mejorar con un DTO de Error)
        }
    }

    /**
     * PUT /api/v1/contabilidad/plan-de-cuentas/{id}
     * Actualiza una cuenta.
     */
    @PutMapping("/{id}")
    // TODO: @PreAuthorize("hasRole('ROLE_CONTABILIDAD')")
    public ResponseEntity<PlanDeCuentasDTO> actualizarCuenta(@PathVariable Long id, @Valid @RequestBody PlanDeCuentasDTO cuentaDTO) {
        try {
            PlanDeCuentasDTO cuentaActualizada = planDeCuentasService.actualizarCuenta(id, cuentaDTO);
            return ResponseEntity.ok(cuentaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // (Mejorar con un DTO de Error)
        }
    }

    /**
     * DELETE /api/v1/contabilidad/plan-de-cuentas/{id}
     * Elimina una cuenta.
     */
    @DeleteMapping("/{id}")
    // TODO: @PreAuthorize("hasRole('ROLE_CONTABILIDAD')")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id) {
        try {
            planDeCuentasService.eliminarCuenta(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            // Ej. No existe, o tiene hijos
            return ResponseEntity.badRequest().build(); // (Mejorar con un DTO de Error)
        }
    }
}
