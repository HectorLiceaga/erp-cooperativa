package com.cooperativa.erp.contabilidad.dto;

import com.cooperativa.erp.contabilidad.entity.PlanDeCuentas;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO para la entidad PlanDeCuentas, usando los nombres de campo correctos
 * (nombre, cuentaPadre, cuentasHijas).
 */
@Data
@NoArgsConstructor
public class PlanDeCuentasDTO {

    private Long id;

    @NotBlank(message = "El código no puede estar vacío")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)*$", message = "El código debe seguir un formato numérico separado por puntos (ej. 1.01.01.001)")
    private String codigo;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotNull(message = "Debe especificar si la cuenta es imputable")
    private Boolean imputable;

    private Long padreId; // ID de la cuenta padre
    private String padreNombre; // Nombre de la cuenta padre (para visualización)
    private Set<String> hijas; // Set de nombres o códigos de las hijas (simple)

    /**
     * Constructor para convertir de Entidad a DTO.
     * USA LOS GETTERS CORRECTOS: getNombre(), getCuentaPadre(), getCuentasHijas()
     */
    public PlanDeCuentasDTO(PlanDeCuentas cuenta) {
        this.id = cuenta.getId();
        this.codigo = cuenta.getCodigo();
        this.nombre = cuenta.getNombre(); // Usa getNombre()
        this.imputable = cuenta.getImputable();

        // Mapeo seguro del Padre
        if (cuenta.getCuentaPadre() != null) { // Usa getCuentaPadre()
            this.padreId = cuenta.getCuentaPadre().getId();
            this.padreNombre = cuenta.getCuentaPadre().getNombre(); // Usa getNombre()
        }

        // Mapeo seguro de las Hijas
        if (cuenta.getCuentasHijas() != null && !cuenta.getCuentasHijas().isEmpty()) { // Usa getCuentasHijas()
            this.hijas = cuenta.getCuentasHijas().stream()
                    .map(PlanDeCuentas::getNombre) // Usa getNombre()
                    .collect(Collectors.toSet());
        } else {
            this.hijas = Collections.emptySet();
        }
    }
}

