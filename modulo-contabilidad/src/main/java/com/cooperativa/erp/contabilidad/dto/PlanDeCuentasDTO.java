package com.cooperativa.erp.contabilidad.dto;

import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO para crear, actualizar y visualizar cuentas del Plan de Cuentas.
 * Simplifica la estructura de árbol para la API.
 */
@Data
@NoArgsConstructor
public class PlanDeCuentasDTO {

    private Long id;

    @NotBlank(message = "El código no puede estar vacío")
    @Size(max = 20)
    private String codigo;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 100)
    private String descripcion;

    @NotNull(message = "Debe especificar si la cuenta es imputable")
    private Boolean imputable;

    // Usamos el ID del padre para crear/actualizar
    private Long padreId;

    // Usamos DTOs anidados para visualizar los hijos
    private Set<PlanDeCuentasDTO> hijos;

    /**
     * Constructor de conversión (Entity -> DTO)
     * Este constructor es recursivo para construir el árbol.
     */
    public PlanDeCuentasDTO(PlanDeCuentas cuenta, boolean incluirHijos) {
        this.id = cuenta.getId();
        this.codigo = cuenta.getCodigo();
        this.descripcion = cuenta.getDescripcion();
        this.imputable = cuenta.getImputable();

        if (cuenta.getPadre() != null) {
            this.padreId = cuenta.getPadre().getId();
        }

        if (incluirHijos && cuenta.getHijos() != null && !cuenta.getHijos().isEmpty()) {
            this.hijos = cuenta.getHijos().stream()
                    .map(hijo -> new PlanDeCuentasDTO(hijo, true)) // Recursividad
                    .collect(Collectors.toSet());
        }
    }
}
