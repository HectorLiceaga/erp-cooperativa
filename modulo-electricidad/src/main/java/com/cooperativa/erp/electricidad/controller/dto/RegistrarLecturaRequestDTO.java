package com.cooperativa.erp.controller.electricidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la solicitud de registrar una nueva lectura.
 */
@Data
public class RegistrarLecturaRequestDTO {

    @NotNull(message = "El ID del medidor es requerido")
    private Long medidorId;

    @NotNull(message = "La fecha de lectura es requerida")
    @PastOrPresent(message = "La fecha de lectura no puede ser futura")
    private LocalDate fechaLectura;

    @NotNull(message = "El estado es requerido")
    @PositiveOrZero(message = "El estado no puede ser negativo")
    private BigDecimal estado;

    @NotBlank(message = "El tipo de lectura no puede estar vacío")
    private String tipoLectura; // Ej: "Normal", "Estimada", "Retiro"
}

