package com.cooperativa.erp.controller.electricidad.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para representar una Lectura en las respuestas de la API.
 */
@Data
@NoArgsConstructor
public class LecturaDTO {
    private Long id;
    private Long medidorId;
    private String medidorNumero; // Para info útil
    private LocalDate fechaLectura;
    private BigDecimal estado;
    private String tipoLectura;

    // Podríamos añadir aquí el consumo calculado si el servicio lo devuelve
    // private BigDecimal consumo;
}

