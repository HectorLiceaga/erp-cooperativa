package com.cooperativa.erp.electricidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Asumiendo que tenés validaciones (si no, podés borrar los @)
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Positive;
// import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * DTO para registrar una nueva lectura.
 * Esto es lo que el Controller recibirá como JSON en el @RequestBody.
 */
@Data
public class LecturaRequestDTO {

    // @NotNull
    // @Positive
    private Long contratoId;

    // @NotNull
    private BigDecimal estadoActual;

    // @NotNull
    private LocalDate fechaToma;

    // @NotNull
    private LocalDate periodo; // El parámetro que faltaba en el controller viejo

    // @NotNull
    // @NotEmpty
    private String tipoLectura;
}
