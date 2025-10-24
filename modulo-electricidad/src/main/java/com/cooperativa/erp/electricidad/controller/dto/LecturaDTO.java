package com.cooperativa.erp.controller.electricidad.dto;

import lombok.AllArgsConstructor; // Añadir para constructor con todos los args
import lombok.Data;             // Añadir para getters, setters, toString, etc.
import lombok.NoArgsConstructor;  // Añadir para constructor vacío (buena práctica)

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para devolver información sobre una Lectura.
 */
@Data // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class LecturaDTO {

    private Long id;
    private Long medidorId;
    private String medidorNumero; // Incluimos el número para conveniencia
    private LocalDate fechaLectura;
    private BigDecimal estado;
    private String tipoLectura;

    // Los getters, setters y constructores son generados por Lombok
}

