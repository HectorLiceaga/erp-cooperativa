package com.cooperativa.erp.controller.electricidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar un Medidor en las respuestas de la API.
 * También se usa como base para la creación/actualización.
 */
@Data
@NoArgsConstructor
public class MedidorDTO {

    private Long id; // Incluido en respuestas

    @NotBlank(message = "El número de medidor no puede estar vacío")
    private String numero;

    @NotBlank(message = "La marca no puede estar vacía")
    private String marca;

    @NotBlank(message = "El modelo no puede estar vacío")
    private String modelo;

    @NotNull(message = "La constante de multiplicación es requerida")
    @PositiveOrZero(message = "La constante debe ser cero o positiva")
    private BigDecimal constanteMultiplicacion;

    // Podríamos incluir el ID del suministro si está asociado
    private Long suministroId;
    private String suministroDireccion; // Para mostrar algo útil
}

